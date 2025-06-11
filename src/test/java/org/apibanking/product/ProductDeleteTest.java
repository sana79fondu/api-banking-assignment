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
public class ProductDeleteTest {

    @Test
    public void testDeleteSuccess() {
        // Create a product
        Product product = new Product();
        product.name = "ToDelete";
        product.description = "Temp product";
        product.price = 20.0;
        product.quantity = 1;

        Long id = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Delete the product
        given()
                .when()
                .delete("/" + id)
                .then()
                .statusCode(204); // No Content

        // Ensure it is really deleted
        given()
                .when()
                .get("/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteNotFound() {
        given()
                .when()
                .delete("/10")
                .then()
                .statusCode(404)
                .body(equalTo("Product not found with ID: 10"));
    }

    @Test
    public void testDeleteInvalidId() {
        given()
                .when()
                .delete("/0")
                .then()
                .statusCode(400)
                .body(equalTo("Product ID must be a positive number"));
    }
}