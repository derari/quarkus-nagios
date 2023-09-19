package org.cthul.nagios.quarkus.extension.runtime;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.cthul.nagios.health.NagiosCheckResponse;
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

    @ActivateRequestContext
    NagiosCheckResponse checkInContext(RoutingContext context) {
        User user = context.user();
        if (user instanceof QuarkusHttpUser && identityAssociation.isResolvable()) {
            QuarkusHttpUser quarkusUser = (QuarkusHttpUser) user;
            identityAssociation.get().setIdentity(quarkusUser.getSecurityIdentity());
        }
        return check();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public NagiosCheckResponse check() {
        try (InstanceHandle<SmallRyeHealthReporter> srHealthHandle = Arc.container().instance(SmallRyeHealthReporter.class)) {
            srHealthHandle.get().getHealth();
        }
        return Uni.combine().all()
                .unis(getHealthResponses())
                .combinedWith(responses -> NagiosCheckResponse.named("Report").withChecks((List) responses).build())
                .await().atMost(Duration.ofMillis(90000));
    }

    private List<Uni<HealthCheckResponse>> getHealthResponses() {
        List<Uni<HealthCheckResponse>> result = new ArrayList<>();
        addHealthResponses(checks.handlesStream(), check -> result.add(asyncHealthCheckFactory.callSync(check)));
        addHealthResponses(asyncChecks.handlesStream(), check -> result.add(asyncHealthCheckFactory.callAsync(check)));
        addHealthResponses(HealthRegistries.getRegistry(HealthType.WELLNESS), result);
        HealthRegistries.getHealthGroupRegistries().forEach(reg -> addHealthResponses(reg, result));
        return result;
    }

    private <T> void addHealthResponses(Stream<? extends Instance.Handle<T>> stream, Consumer<T> action) {
        stream.filter(this::isWellnessOrGroup).map(Instance.Handle::get).forEach(action);
    }

    private boolean isWellnessOrGroup(Instance.Handle<?> handle) {
        return handle.getBean().getQualifiers().stream()
                .anyMatch(at -> at instanceof Wellness || at instanceof HealthGroup || at instanceof HealthGroups);
    }

    private void addHealthResponses(HealthRegistry registry, List<Uni<HealthCheckResponse>> result) {
        if (registry instanceof HealthRegistryImpl) {
            result.addAll(((HealthRegistryImpl) registry).getChecks(Map.of()));
        }
    }
}
