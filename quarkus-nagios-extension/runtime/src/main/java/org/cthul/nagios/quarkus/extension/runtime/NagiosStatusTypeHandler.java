package org.cthul.nagios.quarkus.extension.runtime;

import static org.cthul.nagios.quarkus.extension.runtime.NagiosStatusReporter.HG_ALL;
import static org.cthul.nagios.quarkus.extension.runtime.NagiosStatusReporter.HG_GROUP;

import io.smallrye.health.api.HealthType;
import io.vertx.ext.web.RoutingContext;

public class NagiosStatusTypeHandler extends NagiosStatusGroupHandler {

    @Override
    protected HealthType getType(RoutingContext context) {
        switch (super.getGroup(context)) {
            case "well":
                return HealthType.WELLNESS;
            case "ready":
                return HealthType.READINESS;
            case "live":
                return HealthType.LIVENESS;
            case "started":
                return HealthType.STARTUP;
            default:
                return null;
        }
    }

    @Override
    protected String getGroup(RoutingContext context) {
        String group = super.getGroup(context);
        if ("all".equals(group))
            return HG_ALL;
        if ("group".equals(group))
            return HG_GROUP;
        return group;
    }
}
