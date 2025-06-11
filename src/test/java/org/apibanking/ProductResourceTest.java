package org.apibanking;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apibanking.entity.Product;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ProductResourceTest {
    @Test
    void testHealthCheckEndpoint() {
        given()
          .when()
                .get("/api/healthcheck")
          .then().log().all()
             .statusCode(200)
             .body(is("Healthy"));
    }
}