package org.apibanking.product;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.apibanking.entity.Product;
import org.apibanking.resource.ProductResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductGetByIdTest {
    @Test
//    @Transactional
//    @TestTransaction
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
    @Transactional
    @TestTransaction
    public void testGetByIdNotFound() {
        given()
                .when()
                .get("/10")
                .then()
                .statusCode(404)
                .body(equalTo("Product not found for ID: 10"));
    }

    @Test
    @Transactional
    @TestTransaction
    public void testGetByIdWithInvalidId() {
        given()
                .when()
                .get("/0")
                .then()
                .statusCode(400)
                .body(equalTo("Product ID must be a positive number"));
    }
}
