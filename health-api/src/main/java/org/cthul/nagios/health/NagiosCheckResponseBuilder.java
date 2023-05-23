package org.cthul.nagios.health;

import java.util.*;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public class NagiosCheckResponseBuilder extends HealthCheckResponseBuilder {

    private final List<NagiosCheckResult> results = new ArrayList<>();
    private final Map<String, Object> data = new LinkedHashMap<>();
    private String name;
    private NagiosStatus status = null;
    private int count = 0;

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
        results.add(check);
        data.put(check.getLabel(), check.info());
        count++;
        return this;
    }

    public NagiosCheckResponseBuilder withCheck(NagiosCheckResponse check) {
        results.addAll(check.getResults());
        check.getData().ifPresent(data::putAll);
        count += check.getNumberOfChecks();
        return this;
    }

    public NagiosCheckResponseBuilder withCheck(HealthCheckResponse check) {
        if (check instanceof NagiosCheckResponse nagios) {
            return withCheck(nagios);
        }
        var status = NagiosStatus.ofHealth(check.getStatus());
        var result = new NagiosCheckResult(check.getName(), status);
        results.add(result);
        check.getData().ifPresentOrElse(data::putAll,
                () -> data.put(result.getLabel(), result.info()));
        count += check.getData().map(Map::size).orElse(1);
        return this;
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
        var finalCount = count == 0 ? Math.min(data.size(), 1) : count;
        var finalResults = new ArrayList<NagiosCheckResult>();
        var finalData = new HashMap<String, Object>();
        var subresultStatus = getSubresultsStatus();
        var statusCheck = getStatusCheck(subresultStatus);
        if (statusCheck != null) {
            finalResults.add(statusCheck);
            finalData.put(statusCheck.getLabel(), statusCheck.info());
            subresultStatus = subresultStatus.and(statusCheck.status());
        }
        finalResults.addAll(results);
        finalData.putAll(data);
        return new NagiosCheckResponse(name, subresultStatus, finalCount, finalResults, finalData);
    }

    private NagiosCheckResult getStatusCheck(NagiosStatus subresultStatus) {
        if (requiresExplicitStatusCheck(subresultStatus)) {
            return new NagiosCheckResult(name, Objects.requireNonNullElse(status, NagiosStatus.OK));
        }
        return null;
    }

    private boolean requiresExplicitStatusCheck(NagiosStatus subresultStatus) {
        if (results.isEmpty())
            return true;
        return status != null && status.and(subresultStatus) != subresultStatus;
    }

    private NagiosStatus getSubresultsStatus() {
        return results.stream()
                .map(NagiosCheckResult::status)
                .reduce(NagiosStatus::and)
                .orElse(NagiosStatus.OK);
    }
}
