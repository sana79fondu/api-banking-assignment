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
public class ProductStockTest {

    @Test
    public void testStockAvailable() {
        Product product = new Product();
        product.name = "StockCheck";
        product.description = "Stock test product";
        product.price = 25.0;
        product.quantity = 10;

        Long id = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .when()
                .get("/" + id + "/stock?count=5")
                .then()
                .statusCode(200)
                .body(equalTo("true"));
    }

    @Test
    public void testStockNotAvailable() {
        Product product = new Product();
        product.name = "StockFail";
        product.description = "Limited stock";
        product.price = 10.0;
        product.quantity = 3;

        Long id = given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .when()
                .get("/" + id + "/stock?count=5")
                .then()
                .statusCode(200)
                .body(equalTo("false"));
    }

    @Test
    public void testStockProductNotFound() {
        given()
                .when()
                .get("/10/stock?count=5")
                .then()
                .statusCode(404)
                .body(equalTo("Product not found for ID: 10"));
    }

    @Test
    public void testInvalidIdOrCount() {
        given()
                .when()
                .get("/0/stock?count=5")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid product ID"));

        given()
                .when()
                .get("/1/stock?count=0")
                .then()
                .statusCode(400)
                .body(equalTo("Count must be positive"));
    }
}