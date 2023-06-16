package org.cthul.nagios.health;

import java.util.List;
import java.util.Map;

public interface NagiosCheckResult {

    String getName();

    NagiosStatus getNagiosStatus();

    StringBuilder describeResult(StringBuilder sb);

    StringBuilder describeStatus(StringBuilder sb);

    default String getStatusString() {
        return describeStatus(new StringBuilder()).toString();
    }

    Map<String, Object> getData();

    List<NagiosPerformanceValue> getPerformanceValues();

    default NagiosCheckResponse asResponse() {
        return NagiosCheckResponse.named(getName()).withCheck(this).build();
    }
}
