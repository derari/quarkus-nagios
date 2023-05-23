package org.cthul.nagios.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class NagiosCheckResultTest {

    @Test
    void warning_above() {
        var key = NagiosCheckKey.labelled("test").warning().above(10).build();
        var ok = key.value(10);
        var warn = key.value(11);
        assertEquals(NagiosStatus.OK, ok.status());
        assertEquals(NagiosStatus.WARNING, warn.status());
        assertEquals("10", key.getWarningExpression().orElse(""));
    }
}