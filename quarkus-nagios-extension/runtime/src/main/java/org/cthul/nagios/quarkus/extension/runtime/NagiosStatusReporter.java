package org.cthul.nagios.quarkus.extension.runtime;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.cthul.nagios.health.NagiosCheckResponse;
import org.cthul.nagios.health.NagiosStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.*;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.smallrye.health.AsyncHealthCheckFactory;
import io.smallrye.health.SmallRyeHealthReporter;
import io.smallrye.health.api.*;
import io.smallrye.health.registry.HealthRegistries;
import io.smallrye.health.registry.HealthRegistryImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class NagiosStatusReporter {

    public static final String HG_GROUP = "/group/";
    public static final String HG_WELL_OR_GROUP = "/well/or/group/";
    public static final String HG_ALL = "/all/";

    @Inject
    @Any
    Instance<HealthCheck> checks;

    @Inject
    @Any
    Instance<AsyncHealthCheck> asyncChecks;

    @Inject
    AsyncHealthCheckFactory asyncHealthCheckFactory;

    @Inject
    Instance<CurrentIdentityAssociation> identityAssociation;

    @ConfigProperty(name = "quarkus.nagios.default-group", defaultValue = HG_WELL_OR_GROUP)
    String defaultGroup;

    boolean initialized = false;

    @ActivateRequestContext
    NagiosCheckResponse checkInContext(RoutingContext context, HealthType type, String group) {
        User user = context.user();
        if (user instanceof QuarkusHttpUser && identityAssociation.isResolvable()) {
            QuarkusHttpUser quarkusUser = (QuarkusHttpUser) user;
            identityAssociation.get().setIdentity(quarkusUser.getSecurityIdentity());
        }
        return check(type, group);
    }

    public NagiosCheckResponse check(HealthType type, String group) {
        init();
        if (type == null && group == null)
            group = defaultGroup;
        return Uni.combine().all()
                .unis(getHealthResponses(type, group))
                .combinedWith(HealthCheckResponse.class,
                        responses -> NagiosCheckResponse.named("Report").withChecks(responses).build())
                .await().atMost(Duration.ofMillis(90000));
    }

    private void init() {
        if (initialized)
            return;
        try (InstanceHandle<SmallRyeHealthReporter> srHealthHandle = Arc.container().instance(SmallRyeHealthReporter.class)) {
            srHealthHandle.get().getHealth();
        }
        initialized = true;
    }

    private List<Uni<HealthCheckResponse>> getHealthResponses(HealthType type, String group) {
        List<Uni<HealthCheckResponse>> result = new ArrayList<>();
        Predicate<Instance.Handle<?>> includes = includes(type, group);
        addHealthResponses(checks.handlesStream(), includes, check -> result.add(asyncHealthCheckFactory.callSync(check)));
        addHealthResponses(asyncChecks.handlesStream(), includes,
                check -> result.add(asyncHealthCheckFactory.callAsync(check)));
        addHealthResponses(getRegistries(type, group), result);
        if (result.isEmpty()) {
            result.add(Uni.createFrom().item(NagiosCheckResponse.named("not found").status(NagiosStatus.UNKNOWN).build()));
        }
        return result;
    }

    private Predicate<Instance.Handle<?>> includes(HealthType type, String group) {
        return findInstances(type, group)
                .and(h -> qualifiers(h).noneMatch(NagiosExcluded.class::isInstance));
    }

    private Predicate<Instance.Handle<?>> findInstances(HealthType type, String group) {
        if (type != null)
            return findInstances(type);
        if (group.equals(HG_WELL_OR_GROUP))
            return findInstances(HealthType.WELLNESS).or(includes(null, HG_GROUP));
        if (group.equals(HG_ALL))
            return h -> true;
        if (group.equals(HG_GROUP))
            return h -> groups(h).anyMatch(hg -> true);
        return h -> groups(h).anyMatch(hg -> group.equals(hg.value()));
    }

    private Predicate<Instance.Handle<?>> findInstances(HealthType type) {
        switch (type) {
            case WELLNESS:
                return handle -> qualifiers(handle).anyMatch(Wellness.class::isInstance);
            case READINESS:
                return handle -> qualifiers(handle).anyMatch(Readiness.class::isInstance);
            case LIVENESS:
                return handle -> qualifiers(handle).anyMatch(Liveness.class::isInstance);
            case STARTUP:
                return handle -> qualifiers(handle).anyMatch(Startup.class::isInstance);
            default:
                throw new IllegalArgumentException("" + type);
        }
    }

    private Stream<Annotation> qualifiers(Instance.Handle<?> handle) {
        return handle.getBean().getQualifiers().stream();
    }

    private Stream<HealthGroup> groups(Instance.Handle<?> handle) {
        return qualifiers(handle)
                .flatMap(at -> {
                    if (at instanceof HealthGroup) {
                        return Stream.of((HealthGroup) at);
                    }
                    if (at instanceof HealthGroups) {
                        return Stream.of(((HealthGroups) at).value());
                    }
                    return Stream.empty();
                });
    }

    private <T> void addHealthResponses(Stream<? extends Instance.Handle<T>> stream, Predicate<Instance.Handle<?>> includes,
            Consumer<T> action) {
        stream.filter(includes).map(Instance.Handle::get).forEach(action);
    }

    private Collection<HealthRegistry> getRegistries(HealthType type, String group) {
        if (type != null) {
            return List.of(HealthRegistries.getRegistry(type));
        }
        if (group.equals(HG_WELL_OR_GROUP)) {
            List<HealthRegistry> result = new ArrayList<>();
            result.add(HealthRegistries.getRegistry(HealthType.WELLNESS));
            result.addAll(HealthRegistries.getHealthGroupRegistries());
            return result;
        }
        if (group.equals(HG_ALL)) {
            List<HealthRegistry> result = new ArrayList<>();
            result.add(HealthRegistries.getRegistry(HealthType.WELLNESS));
            result.add(HealthRegistries.getRegistry(HealthType.READINESS));
            result.add(HealthRegistries.getRegistry(HealthType.LIVENESS));
            result.add(HealthRegistries.getRegistry(HealthType.STARTUP));
            result.addAll(HealthRegistries.getHealthGroupRegistries());
            return result;
        }
        if (group.contains(HG_GROUP)) {
            return HealthRegistries.getHealthGroupRegistries();
        }
        return List.of(HealthRegistries.getHealthGroupRegistry(group));
    }

    private void addHealthResponses(Collection<HealthRegistry> registries, List<Uni<HealthCheckResponse>> result) {
        for (HealthRegistry registry : registries) {
            if (registry instanceof HealthRegistryImpl) {
                result.addAll(((HealthRegistryImpl) registry).getChecks(Map.of()));
            }
        }
    }
}
