package org.cthul.nagios.health;

import java.util.function.Function;

public class NagiosCheckKeyBuilder {
    
    private String label = "health";
    private String unit;
    private NagiosCheckKey.AlertRange warningRange = NagiosCheckKey.AlertRange.ALLOW_POSITIVE;
    private NagiosCheckKey.AlertRange criticalRange = NagiosCheckKey.AlertRange.ALLOW_POSITIVE;

    public NagiosCheckKeyBuilder label(String label) {
        this.label = label;
        return this;
    }

    public NagiosCheckKeyBuilder unit(String unit) {
        this.unit = unit;
        return this;
    }

    public NagiosCheckKeyBuilder warning(NagiosCheckKey.AlertRange warningRange) {
        this.warningRange = warningRange;
        return this;
    }

    public NagiosCheckKeyBuilder critical(NagiosCheckKey.AlertRange criticalRange) {
        this.criticalRange = criticalRange;
        return this;
    }

    public RangeBuilder<NagiosCheckKeyBuilder> warning() {
        return new RangeBuilder<>(this::warning);
    }

    public RangeBuilder<NagiosCheckKeyBuilder> critical() {
        return new RangeBuilder<>(this::critical);
    }

    public NagiosCheckKey build() {
        return new NagiosCheckKey(label, unit, warningRange, criticalRange);
    }

    public static class RangeBuilder<T> {

        private long min = 0;
        private long max = Long.MAX_VALUE;
        private boolean inside = false;

        private final Function<NagiosCheckKey.AlertRange, T> setter;

        public RangeBuilder(Function<NagiosCheckKey.AlertRange, T> setter) {
            this.setter = setter;
        }

        public RangeBuilder<T> inside() {
            this.inside = true;
            return this;
        }

        public RangeBuilder<T> outside() {
            this.inside = false;
            return this;
        }

        public RangeBuilder<T> negative() {
            this.min = Long.MIN_VALUE;
            return this;
        }

        public T range(long min, long max) {
            this.min = min;
            this.max = max;
            return build();
        }

        public T negativeOrAbove(long max) {
            return outside().range(0, max);
        }

        public T above(long max) {
            return outside().range(min, max);
        }

        public T below(long min) {
            return outside().range(min, Long.MAX_VALUE);
        }

        public T inside(long min, long max) {
            return inside().range(min, max);
        }

        public T outside(long min, long max) {
            return outside().range(min, max);
        }

        public T build() {
            return setter.apply(new NagiosCheckKey.AlertRange(min, max, inside));
        }
    }
}
