package com.amadeus.aud.services.engine.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
@QuarkusTestResource(ConnectivityEngineOidcTestResource.class)
class HealthResourceIT {

  @Test
  void anonymousUserCanAccessHealth() {
    given()
        .when()
        .get("/health")
        .then()
        .statusCode(200)
        .header("Content-Type", startsWith("text/plain"))
        .body(equalTo("Still alive"));
  }
}
