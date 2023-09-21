package org.cthul.nagios.quarkus.extension.runtime;

import io.smallrye.health.api.HealthType;
import io.vertx.ext.web.RoutingContext;

public class NagiosStatusGroupHandler extends NagiosStatusRootHandler {

    protected HealthType getType(RoutingContext context) {
        return null;
    }

    protected String getGroup(RoutingContext context) {
        String path = context.normalizedPath();
        int end = path.length();
        int start = path.lastIndexOf('/');
        if (start + 1 == path.length()) {
            end--;
            start = path.lastIndexOf('/', start - 1);
        }
        if (start < 0)
            return "";
        return path.substring(start + 1, end);
    }
}
