package org.cthul.nagios.health;

import org.eclipse.microprofile.health.HealthCheckResponse;

import java.util.*;

public class NagiosCheckResponse extends HealthCheckResponse {

    public static NagiosCheckResponseBuilder named(String name) {
        return new NagiosCheckResponseBuilder().name(name);
    }

    private final NagiosStatus status;
    private final List<NagiosCheckResult> checks;
    private final List<NagiosPerformanceValue> performance;

    public NagiosCheckResponse(String name, NagiosStatus status, List<NagiosCheckResult> checks, List<NagiosPerformanceValue> performance, Map<String, Object> data) {
        super(name, status.toHealth(), Optional.of(data));
        this.status = status;
        this.checks = checks;
        this.performance = performance;
    }

    public NagiosStatus getNagiosStatus() {
        return status;
    }

    public List<NagiosCheckResult> getChecks() {
        return checks;
    }

    public List<NagiosPerformanceValue> getPerformanceValues() {
        return performance;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(status).append(": ");
        describeInfo(sb).append('|');
        performance.stream()
                .sorted(Comparator.comparing(NagiosPerformanceValue::getLabel))
                .forEach(p -> p.describeRecord(sb).append(' '));
        sb.setLength(sb.length() - 1);
        sb.append('\n');
        getData().ifPresent(map -> map.forEach((key, value) -> sb.append(key).append(": ").append(value).append('\n')));
        return sb.toString();
    }

    private StringBuilder describeInfo(StringBuilder sb) {
        if (status == NagiosStatus.OK) {
            return sb.append(Math.min(1, checks.size())).append(" checks passed");
        }
        var matching = checks.stream().filter(r -> r.getNagiosStatus() == status).toList();
        if (matching.isEmpty()) {
            return sb.append(getName());
        }
        if (matching.size() > 3) {
            return sb.append(matching.size()).append(' ')
                    .append(status.toString().toLowerCase()).append('s');
        }
        var result = new StringBuilder();
        matching.stream()
                .sorted(Comparator.comparing(NagiosCheckResult::getName))
                .forEach(check -> check.describeResult(result).append("; "));
        return sb.append(result.toString().replace('|', '/'));
    }
}
