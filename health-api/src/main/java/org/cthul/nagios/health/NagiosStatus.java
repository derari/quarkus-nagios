package org.cthul.nagios.health;

import org.eclipse.microprofile.health.HealthCheckResponse;

public enum NagiosStatus {

    OK,
    UNKNOWN,
    WARNING,
    CRITICAL;

    public NagiosStatus and(NagiosStatus other) {
        int n = Math.min(ordinal(), other.ordinal());
        return VALUES[n];
    }

    public HealthCheckResponse.Status toHealth() {
        return this == CRITICAL ? HealthCheckResponse.Status.DOWN : HealthCheckResponse.Status.UP;
    }

    public static NagiosStatus ofHealth(HealthCheckResponse.Status status) {
        return status == HealthCheckResponse.Status.UP ? OK : CRITICAL;
    }

    private static final NagiosStatus[] VALUES = NagiosStatus.values();
}
