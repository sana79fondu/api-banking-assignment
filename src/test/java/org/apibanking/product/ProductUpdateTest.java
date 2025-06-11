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
public class ProductUpdateTest {

    @Test
    public void testUpdateSuccess() {
        // 1. Create a product
        Product product = new Product();
        product.name = "PatchTest";
        product.description = "Original Description";
        product.price = 99.0;
        product.quantity = 3;

        Long id = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // 2. Send partial update (only quantity)
        Product partialUpdate = new Product();
        partialUpdate.quantity = 10; // only quantity updated

        given()
                .contentType(ContentType.JSON)
                .body(partialUpdate)
                .when()
                .put("/" + id)
                .then()
                .statusCode(200)
                .body("quantity", equalTo(10))
                .body("name", equalTo("PatchTest"))
                .body("description", equalTo("Original Description"));
    }

    @Test
    public void testNotFound() {
        Product partialUpdate = new Product();
        partialUpdate.quantity = 1;

        given()
                .contentType(ContentType.JSON)
                .body(partialUpdate)
                .when()
                .put("/10")
                .then()
                .statusCode(404)
                .body(equalTo("Product not found with ID: 10"));
    }

    @Test
    public void testUpdateWithInvalidId() {
        Product partialUpdate = new Product();
        partialUpdate.quantity = 1;

        given()
                .contentType(ContentType.JSON)
                .body(partialUpdate)
                .when()
                .put("/0")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid product ID"));
    }


}
