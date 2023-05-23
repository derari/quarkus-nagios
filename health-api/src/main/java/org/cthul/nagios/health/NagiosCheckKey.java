package org.cthul.nagios.health;

import java.util.Optional;

public record NagiosCheckKey(
        String label,
        String unit,
        AlertRange warningRange,
        AlertRange criticalRange) {

    public static NagiosCheckKeyBuilder labelled(String label) {
        return new NagiosCheckKeyBuilder().label(label);
    }

    public static NagiosCheckKey status(String name) {
        return new NagiosCheckKey(name, null, AlertRange.ALLOW_ZERO, new AlertRange(1));
    }

    public Optional<String> getUnit() {
        return Optional.ofNullable(unit);
    }

    public Optional<String> getWarningExpression() {
        return warningRange.getExpression();
    }

    public Optional<String> getCriticalExpression() {
        return criticalRange.getExpression();
    }

    public NagiosStatus status(long value) {
        return criticalRange().alert(value) ? NagiosStatus.CRITICAL
                : warningRange().alert(value) ? NagiosStatus.WARNING : NagiosStatus.OK;
    }

    public NagiosCheckResult value(long value) {
        return new NagiosCheckResult(this, value);
    }

    public NagiosCheckResult value(long value, String info) {
        return new NagiosCheckResult(this, value, info);
    }

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
    }
}
