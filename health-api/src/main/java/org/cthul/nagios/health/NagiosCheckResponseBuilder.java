package org.cthul.nagios.health;

import java.util.*;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public class NagiosCheckResponseBuilder extends HealthCheckResponseBuilder {

    private String name;
    private NagiosStatus status = null;
    private final List<NagiosCheckResult> checks = new ArrayList<>();
    private final List<NagiosPerformanceValue> performance = new ArrayList<>();
    private final Map<String, Object> data = new LinkedHashMap<>();

    @Override
    public NagiosCheckResponseBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder withData(String key, String value) {
        data.put(key, value);
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder withData(String key, long value) {
        data.put(key, value);
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder withData(String key, boolean value) {
        data.put(key, value);
        return this;
    }

    public NagiosCheckResponseBuilder withCheck(NagiosCheckResult check) {
        checks.add(check);
        performance.addAll(check.getPerformanceValues());
        data.put(check.getName(), check.getStatusString());
        data.putAll(check.getData());
        return this;
    }

    public NagiosCheckResponseBuilder withCheck(NagiosCheckResponse response) {
        checks.addAll(response.getChecks());
        performance.addAll(response.getPerformanceValues());
        data.put(response.getName(), response.getNagiosStatus());
        response.getData().ifPresent(data::putAll);
        return this;
    }

    public NagiosCheckResponseBuilder withCheck(HealthCheckResponse check) {
        if (check instanceof NagiosCheckResponse nagios) {
            return withCheck(nagios);
        }
        return withCheck(new NagiosValueResult(
                check.getName(), check.getStatus(),
                NagiosStatus.ofHealth(check.getStatus()),
                check.getData().orElse(Map.of())
        ));
    }

    public NagiosCheckResponseBuilder withChecks(Iterable<? extends HealthCheckResponse> checks) {
        checks.forEach(this::withCheck);
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder up() {
        status = NagiosStatus.OK;
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder down() {
        status = NagiosStatus.CRITICAL;
        return this;
    }

    public NagiosCheckResponseBuilder warn() {
        status = NagiosStatus.WARNING;
        return this;
    }

    public NagiosCheckResponseBuilder critical() {
        status = NagiosStatus.CRITICAL;
        return this;
    }

    public NagiosCheckResponseBuilder warn(boolean warn) {
        status = warn ? NagiosStatus.WARNING : NagiosStatus.OK;
        return this;
    }

    public NagiosCheckResponseBuilder critical(boolean critical) {
        status = critical ? NagiosStatus.CRITICAL : NagiosStatus.OK;
        return this;
    }

    @Override
    public NagiosCheckResponseBuilder status(boolean up) {
        status = up ? NagiosStatus.OK : NagiosStatus.CRITICAL;
        return this;
    }

    public NagiosCheckResponseBuilder status(NagiosStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public NagiosCheckResponse build() {
        var finalChecks = new ArrayList<NagiosCheckResult>();
        var finalData = new HashMap<String, Object>();
        var subresultStatus = getSubresultsStatus();
        var statusCheck = getStatusCheck(subresultStatus);
        if (statusCheck != null) {
            finalChecks.add(statusCheck);
            finalData.put(statusCheck.getName(), statusCheck.getStatusString());
            subresultStatus = subresultStatus.and(statusCheck.getNagiosStatus());
        }
        finalChecks.addAll(checks);
        finalData.putAll(data);
        return new NagiosCheckResponse(name, subresultStatus, finalChecks, new ArrayList<>(performance), finalData);
    }

    private NagiosCheckResult getStatusCheck(NagiosStatus subresultStatus) {
        if (requiresExplicitStatusCheck(subresultStatus)) {
            return new NagiosValueResult(name, Objects.requireNonNullElse(status, NagiosStatus.OK), Map.of());
        }
        return null;
    }

    private boolean requiresExplicitStatusCheck(NagiosStatus subresultStatus) {
        if (checks.isEmpty())
            return true;
        return status != null && status.and(subresultStatus) != subresultStatus;
    }

    private NagiosStatus getSubresultsStatus() {
        return checks.stream()
                .map(NagiosCheckResult::getNagiosStatus)
                .reduce(NagiosStatus::and)
                .orElse(NagiosStatus.OK);
    }
}
