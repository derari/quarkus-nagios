package org.cthul.nagios.health;

import java.util.function.Function;

public class NagiosCheckBuilder {
    
    private String name = "health";
    private String unit = "";
    private AlertRange warningRange = AlertRange.ALLOW_POSITIVE;
    private AlertRange criticalRange = AlertRange.ALLOW_POSITIVE;
    private boolean exportPerformance = false;

    public NagiosCheckBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NagiosCheckBuilder performance() {
        this.exportPerformance = true;
        return this;
    }

    public NagiosCheckBuilder unit(String unit) {
        this.exportPerformance = true;
        this.unit = unit;
        return this;
    }

    public NagiosCheckBuilder warning(AlertRange warningRange) {
        this.warningRange = warningRange;
        return this;
    }

    public NagiosCheckBuilder critical(AlertRange criticalRange) {
        this.criticalRange = criticalRange;
        return this;
    }

    public RangeBuilder<NagiosCheckBuilder> warningIf() {
        return new RangeBuilder<>(this::warning);
    }

    public RangeBuilder<NagiosCheckBuilder> criticalIf() {
        return new RangeBuilder<>(this::critical);
    }

    public NagiosCheck build() {
        return new NagiosCheck(name, unit, warningRange, criticalRange, exportPerformance);
    }

    public NagiosCheckResult result(long value) {
        return build().result(value);
    }

    public NagiosCheckResult result(long value, NagiosStatus status) {
        return build().result(value, status);
    }

    public NagiosCheckResult result(NagiosStatus status) {
        return build().result(status);
    }

    public NagiosCheckResult result(Object value, NagiosStatus status) {
        return build().result(value, status);
    }

    public static class RangeBuilder<T> {

        private long min = 0;
        private long max = Long.MAX_VALUE;
        private boolean inside = false;

        private final Function<AlertRange, T> setter;

        public RangeBuilder(Function<AlertRange, T> setter) {
            this.setter = setter;
        }

        public RangeBuilder<T> min(long min) {
            this.min = min;
            return this;
        }

        public RangeBuilder<T> max(long max) {
            this.max = max;
            return this;
        }

        public T negativeOrAbove(long max) {
            return outside(0, max);
        }

        public T onlyAbove(long max) {
            return outside(Long.MIN_VALUE, max);
        }

        public T above(long max) {
            return outside(min, max);
        }

        public T below(long min) {
            return outside(min, Long.MAX_VALUE);
        }

        public T inside(long min, long max) {
            this.inside = true;
            this.min = min;
            this.max = max;
            return build();
        }

        public T outside(long min, long max) {
            this.inside = false;
            this.min = min;
            this.max = max;
            return build();
        }

        public T build() {
            return setter.apply(new AlertRange(min, max, inside));
        }
    }
}
