package org.cthul.nagios.health;

import org.eclipse.microprofile.health.HealthCheckResponse;

public enum NagiosStatus {

    OK,
    WARNING,
    CRITICAL,
    UNKNOWN;

    public NagiosStatus and(NagiosStatus other) {
        if (this == CRITICAL || other == CRITICAL)
            return CRITICAL;
        if (this == UNKNOWN || other == UNKNOWN)
            return UNKNOWN;
        if (this == WARNING || other == WARNING)
            return WARNING;
        return OK;
    }

    public HealthCheckResponse.Status toHealth() {
        return this == CRITICAL ? HealthCheckResponse.Status.DOWN : HealthCheckResponse.Status.UP;
    }

    public static NagiosStatus ofHealth(HealthCheckResponse.Status status) {
        return status == HealthCheckResponse.Status.UP ? OK : CRITICAL;
    }

}
