package com.amadeus.aud.services.engine.resources;

import static io.restassured.RestAssured.given;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.services.engine.security.EngineRoles;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.smallrye.jwt.build.Jwt;
import java.util.Set;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
@QuarkusTestResource(ConnectivityEngineOidcTestResource.class)
class GraphResourceAccessIT {
  private static final String TOKEN_ISSUER = "https://server.example.com";

  @Test
  void readRoleCanListVersions() {
    given()
        .auth()
        .oauth2(accessToken("reader", EngineRoles.AUTH_ENGINE_READ_ROLE))
        .when()
        .get("/api/v1/versions")
        .then()
        .statusCode(200);
  }

  @Test
  void writeRoleCannotListVersions() {
    given()
        .auth()
        .oauth2(accessToken("writer", EngineRoles.AUTH_ENGINE_WRITE_ROLE))
        .when()
        .get("/api/v1/versions")
        .then()
        .statusCode(403);
  }

  @Test
  void userWithoutEngineRoleCannotListVersions() {
    given()
        .auth()
        .oauth2(accessToken("user"))
        .when()
        .get("/api/v1/versions")
        .then()
        .statusCode(403);
  }

  @Test
  void unauthenticatedUserCannotListVersions() {
    given().when().get("/api/v1/versions").then().statusCode(401);
  }

  @Test
  void writeRoleReachesInitEndpointWithoutStartingGraphInitialization() {
    given()
        .urlEncodingEnabled(false)
        .auth()
        .oauth2(accessToken("writer", EngineRoles.AUTH_ENGINE_WRITE_ROLE))
        .contentType("application/json")
        .when()
        .post("/api/v1/versions/%20/graph")
        .then()
        .statusCode(400);
  }

  @Test
  void readRoleCannotInitializeGraph() {
    given()
        .auth()
        .oauth2(accessToken("reader", EngineRoles.AUTH_ENGINE_READ_ROLE))
        .contentType("application/json")
        .when()
        .post("/api/v1/versions/latest/graph")
        .then()
        .statusCode(403);
  }

  @Test
  void readRoleCanQueryEdgesByNodeIds() {
    given()
        .auth()
        .oauth2(accessToken("reader", EngineRoles.AUTH_ENGINE_READ_ROLE))
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/v1/versions/0.0.0/graph/edges/by-node-ids")
        .then()
        .statusCode(404);
  }

  @Test
  void malformedVersionReturnsBadRequest() {
    given()
        .auth()
        .oauth2(accessToken("reader", EngineRoles.AUTH_ENGINE_READ_ROLE))
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/v1/versions/missing/graph/edges/by-node-ids")
        .then()
        .statusCode(400);
  }

  @Test
  void writeRoleCannotQueryEdgesByNodeIds() {
    given()
        .auth()
        .oauth2(accessToken("writer", EngineRoles.AUTH_ENGINE_WRITE_ROLE))
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/v1/versions/missing/graph/edges/by-node-ids")
        .then()
        .statusCode(403);
  }

  @Test
  void readAllRoleCannotInitializeGraph() {
    given()
        .auth()
        .oauth2(accessToken("reader", Constants.AUTH_READ_ALL_ROLE))
        .contentType("application/json")
        .when()
        .post("/api/v1/versions/latest/graph")
        .then()
        .statusCode(403);
  }

  @Test
  void unauthenticatedUserCannotInitializeGraph() {
    given()
        .contentType("application/json")
        .when()
        .post("/api/v1/versions/latest/graph")
        .then()
        .statusCode(401);
  }

  private static String accessToken(String userName, String... roles) {
    return Jwt.preferredUserName(userName)
        .claim("roles", Set.of(roles))
        .issuer(TOKEN_ISSUER)
        .audience(TOKEN_ISSUER)
        .subject(userName)
        .jws()
        .keyId("1")
        .sign("privateKey.jwk");
  }
}
