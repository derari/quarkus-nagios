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
        getExpression().ifPresent(sb::append);
        return sb;
    }

    public Optional<String> getExpression() {
        var prefix = inside ? "@" : "";
        if (min == 0) {
            if (max == Long.MAX_VALUE && prefix.isEmpty())
                return Optional.empty();
            return Optional.of(prefix + max);
        }
        var minStr = min == Long.MIN_VALUE ? "~" : String.valueOf(min);
        if (max == Long.MAX_VALUE)
            return Optional.of(prefix + minStr + ":");
        return Optional.of(prefix + minStr + ":" + max);
    }

    public NagiosStatus getStatus(long value, AlertRange warningRange, AlertRange criticalRange) {
        return criticalRange.alert(value) ? NagiosStatus.CRITICAL :
                warningRange.alert(value) ? NagiosStatus.WARNING : NagiosStatus.OK;
    }
}
