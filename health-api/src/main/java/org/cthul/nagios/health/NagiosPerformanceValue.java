package org.cthul.nagios.health;

public interface NagiosPerformanceValue {

    String getLabel();

    long getValue();

    String getUnit();

    StringBuilder describeWarningExpression(StringBuilder sb);

    StringBuilder describeCriticalExpression(StringBuilder sb);

    default StringBuilder describeRecord(StringBuilder sb) {
        sb.append('\'')
            .append(getLabel().replace('\'', '"').replace('=', ':'))
            .append("'=")
            .append(getValue()).append(getUnit())
            .append(';');
        describeWarningExpression(sb).append(';');
        return describeCriticalExpression(sb);
    }
}
