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
        super(name, status.toHealth(), Optional.of(Collections.unmodifiableMap(data)));
        this.status = status;
        this.checks = Collections.unmodifiableList(checks);
        this.performance = Collections.unmodifiableList(performance);
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
            if (checks.size() == 1) return sb.append("1 check passed");
            return sb.append(checks.size()).append(" checks passed");
        }
        var matching = checks.stream().filter(r -> r.getNagiosStatus() == status).toList();
        if (matching.isEmpty()) {
            return sb.append(getName());
        }
        return describeProblems(sb, matching);
    }

    private static StringBuilder describeProblems(StringBuilder sb, List<NagiosCheckResult> matching) {
        var start = sb.length();
        for (int i = 0; i < matching.size() && i < 3; i++) {
            matching.get(i).describeResult(sb).append("; ");
        }
        var end = sb.length() - 2;
        for (int i = start; i < end; i++) {
            if (sb.charAt(i) == '|') sb.setCharAt(i, '/');
        }
        if (matching.size() > 3) {
            return sb.append(matching.size() - 3).append(" more");
        }
        sb.setLength(sb.length() - 2);
        return sb;
    }
}
