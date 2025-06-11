package org.apibanking.product;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apibanking.entity.Product;
import org.apibanking.resource.ProductResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
public class CreateProductTest {
    @Test
    public void testCreateProductSuccess() {
        Product product = new Product();
        product.name = "UniqueProduct";
        product.description = "A new unique product";
        product.price = 100.0;
        product.quantity = 10;

        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/api/add")
                .then()
                .statusCode(201)
                .body("name", equalTo("UniqueProduct"))
                .body("id", notNullValue());
    }

    @Test
    public void testDuplicateProductName() {
        Product product = new Product();
        product.name = "DuplicateProduct";
        product.description = "Duplicate item";
        product.price = 50.0;
        product.quantity = 5;

        // First insert
        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/api/add")
                .then()
                .statusCode(201);

        // Second insert (same name)
        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/api/add")
                .then()
                .statusCode(409)
                .body(equalTo("Product with the same name already exists"));
    }

    @Test
    public void testMissingName() {
        Product product = new Product();
        product.description = "Missing name test";
        product.price = 25.0;
        product.quantity = 1;

        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/api/add")
                .then()
                .statusCode(400)
                .body(equalTo("Product name must not be empty"));
    }

    @Test
    public void testNegativePriceOrQuantity() {
        Product product = new Product();
        product.name = "NegativeTest";
        product.description = "Invalid product";
        product.price = -10.0;
        product.quantity = 5;

        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/api/add")
                .then()
                .statusCode(400)
                .body(equalTo("Price and quantity must be non-negative"));
    }
}
