package org.apibanking.product;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apibanking.entity.Product;
import org.apibanking.resource.ProductResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
public class ProductGetByIdTest {
    @Test
    public void testGetByIdSuccess() {
        Product product = new Product();
        product.name = "FetchTest";
        product.description = "Product to fetch";
        product.price = 100.0;
        product.quantity = 2;

        Long createdId = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .when()
                .get("/" + createdId)
                .then()
                .statusCode(200)
                .body("name", equalTo("FetchTest"));
    }

    @Test
    public void testGetByIdNotFound() {
        given()
                .when()
                .get("/9999999")
                .then()
                .statusCode(404)
                .body(equalTo("Product not found for ID: 9999999"));
    }

    @Test
    public void testGetByIdWithInvalidId() {
        given()
                .when()
                .get("/0")
                .then()
                .statusCode(400)
                .body(equalTo("Product ID must be a positive number"));
    }
}
