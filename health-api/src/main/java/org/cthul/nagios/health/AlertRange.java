package org.cthul.nagios.health;

import java.util.Optional;

public record AlertRange(
        long min,
        long max,
        boolean inside) {

    public static AlertRange ALLOW_ALL = new AlertRange(Long.MIN_VALUE, Long.MAX_VALUE, false);
    public static AlertRange ALLOW_ZERO = new AlertRange(0, 0, false);
    public static AlertRange ALLOW_POSITIVE = new AlertRange(0, Long.MAX_VALUE, false);

    public AlertRange(long max) {
        this(0, max, false);
    }

    public AlertRange {
        if (min > max) throw new IllegalArgumentException(min + " > " + max);
    }

    public boolean alert(long value) {
        return inside ^ (value < min || max < value);
    }

    public StringBuilder describeExpression(StringBuilder sb) {
        if (inside) {
            sb.append('@');
        }
        if (min == Long.MIN_VALUE) {
            sb.append("~:");
        } else if (min != 0) {
            sb.append(min).append(':');
        }
        if (max != Long.MAX_VALUE || (inside && min == 0)) {
            return sb.append(max);
        }
        return sb;
    }

    public Optional<String> getExpression() {
        var sb = describeExpression(new StringBuilder());
        return sb.isEmpty() ? Optional.empty() : Optional.of(sb.toString());
    }

    public NagiosStatus getStatus(long value, AlertRange warningRange, AlertRange criticalRange) {
        if (criticalRange.alert(value)) return NagiosStatus.CRITICAL;
        if (warningRange.alert(value)) return NagiosStatus.WARNING;
        return NagiosStatus.OK;
    }
}
