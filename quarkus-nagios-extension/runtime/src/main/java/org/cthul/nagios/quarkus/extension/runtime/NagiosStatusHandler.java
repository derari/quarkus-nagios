package org.cthul.nagios.quarkus.extension.runtime;

import org.cthul.nagios.health.NagiosCheckResponse;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class NagiosStatusHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        ManagedContext requestContext = Arc.container().requestContext();
        if (requestContext.isActive()) {
            handleInContext(context);
        } else {
            requestContext.activate();
            try {
                handleInContext(context);
            } finally {
                requestContext.terminate();
            }
        }
    }

    private void handleInContext(RoutingContext context) {
        QuarkusHttpUser user = (QuarkusHttpUser) context.user();
        if (user != null) {
            Arc.container().instance(CurrentIdentityAssociation.class).get().setIdentity(user.getSecurityIdentity());
        }
        NagiosStatusReporter reporter = Arc.container().instance(NagiosStatusReporter.class).get();
        NagiosCheckResponse result = reporter.check();
        HttpServerResponse resp = context.response();
        resp.headers().set(HttpHeaders.CONTENT_TYPE, "text/plain+nagios; charset=UTF-8");
        resp.end(result.toString(), "UTF-8");
    }
}
