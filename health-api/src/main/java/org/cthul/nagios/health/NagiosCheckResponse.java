package org.cthul.nagios.health;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.microprofile.health.HealthCheckResponse;

public class NagiosCheckResponse extends HealthCheckResponse {

    public static NagiosCheckResponseBuilder named(String name) {
        return new NagiosCheckResponseBuilder().name(name);
    }

    private final NagiosStatus status;
    private final List<NagiosCheckResult> results;
    private final int count;

    public NagiosCheckResponse(String name, NagiosStatus status, int count, List<NagiosCheckResult> results, Map<String, Object> data) {
        super(name, status == NagiosStatus.CRITICAL ? Status.DOWN : Status.UP, Optional.of(data));
        this.status = status;
        this.results = results;
        this.count = count;
    }

    public NagiosStatus getNagiosStatus() {
        return status;
    }

    public List<NagiosCheckResult> getResults() {
        return results;
    }

    public int getNumberOfChecks() {
        return count;
    }

    public String getInfo() {
        if (status == NagiosStatus.OK) {
            return getNumberOfChecks() + " checks passed";
        }
        var matching = results.stream().filter(r -> r.status() == status).toList();
        if (matching.isEmpty()) {
            return status.toString();
        }
        if (matching.size() > 3) {
            return matching.size() + " " + status.toString().toLowerCase() + "s";
        }
        return matching.stream()
                .map(r -> r.getLabel() + ": " + r.value())
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(status).append(": ")
                .append(getInfo().replace('|', '/'))
                .append("|");
        results.forEach(r -> r.appendData(sb).append(' '));
        sb.setLength(sb.length() - 1);
        sb.append('\n');
        getData().ifPresent(map -> map.forEach((key, value) -> sb.append(key).append(": ").append(value).append('\n')));
        return sb.toString();
    }
}
