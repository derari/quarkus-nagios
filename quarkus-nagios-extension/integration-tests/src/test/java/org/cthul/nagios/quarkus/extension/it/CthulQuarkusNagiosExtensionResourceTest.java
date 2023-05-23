package org.cthul.nagios.quarkus.extension.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CthulQuarkusNagiosExtensionResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/cthul-quarkus-nagios-extension")
                .then()
                .statusCode(200)
                .body(is("Hello cthul-quarkus-nagios-extension"));
    }
}
