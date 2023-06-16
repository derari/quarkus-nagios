package org.cthul.nagios.health;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NagiosCheckResponseBuilderTest {

    @Test
    void withCheck() {
        var response1 = NagiosCheckResponse.named("response 1")
                .withCheck(NagiosCheck.named("check 1").result(0))
                .build();
        var response2 = NagiosCheckResponse.named("response 2")
                .up().build();
        var response3 = NagiosCheckResponse.named("merged")
                .withChecks(List.of(response1, response2))
                .build();
        assertTrue(response3.toString().contains("2 checks passed"));
    }
}