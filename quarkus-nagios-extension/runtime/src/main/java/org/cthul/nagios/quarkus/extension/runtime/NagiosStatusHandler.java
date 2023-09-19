package org.cthul.nagios.quarkus.extension.runtime;

import org.cthul.nagios.health.NagiosCheckResponse;

import io.quarkus.arc.*;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class NagiosStatusHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        try (InstanceHandle<NagiosStatusReporter> handle = Arc.container().instance(NagiosStatusReporter.class)) {
            NagiosCheckResponse result = handle.get().checkInContext(context);
            HttpServerResponse resp = context.response();
            resp.headers().set(HttpHeaders.CONTENT_TYPE, "text/plain+nagios; charset=UTF-8");
            resp.end(result.toString(), "UTF-8");
        }
    }
}
