package org.cthul.nagios.health;

import org.eclipse.microprofile.health.HealthCheckResponse;

import java.util.Objects;

public record NagiosCheckResult(
        NagiosCheckKey key,
        long value,
        String info,
        NagiosStatus status) {

    public NagiosCheckResult(NagiosCheckKey key, long value) {
        this(key, value, key.status(value));
    }

    public NagiosCheckResult(NagiosCheckKey key, long value, String info) {
        this(key, value, info, key.status(value));
    }

    public NagiosCheckResult(NagiosCheckKey key, long value, NagiosStatus status) {
        this(key, value, getInfo(key, value, status), status);
    }

    public NagiosCheckResult(String name, NagiosStatus status) {
        this(NagiosCheckKey.status(name), status.ordinal(), status.toString(), status);
    }

    public String getLabel() {
        return key.label();
    }

    public StringBuilder appendData(StringBuilder sb) {
        sb.append('\'')
                .append(key.label().replace('\'', '"').replace('=', ':'))
                .append("'=")
                .append(value);
        key.getUnit().ifPresent(sb::append);
        sb.append(';');
        key.getWarningExpression().ifPresent(sb::append);
        sb.append(';');
        key.getCriticalExpression().ifPresent(sb::append);
        return sb;
    }

    public String getFullValueString() {
        return value() + Objects.toString(key.unit(), "") + ";"
                + key.getWarningExpression().orElse("") + ";"
                + key.getCriticalExpression().orElse("");
    }

    public NagiosCheckResponseBuilder asResponse() {
        return NagiosCheckResponse.named(getLabel()).withCheck(this);
    }

    private static String getInfo(NagiosCheckKey key, long value, NagiosStatus status) {
        return value + Objects.toString(key.unit(), "")
                + " [" + status + "] ("
                + key.getWarningExpression().orElse("0:") + ";"
                + key.getCriticalExpression().orElse("0:") + ")";
    }
}
