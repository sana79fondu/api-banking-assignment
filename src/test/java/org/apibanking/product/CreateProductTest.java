package org.apibanking.product;

import com.google.inject.Inject;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.restassured.http.ContentType;
import org.apibanking.entity.Product;
import org.apibanking.resource.ProductResource;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import jakarta.transaction.Transactional;
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;

import java.util.List;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
                .post("/add")
                .then()
                .statusCode(201)
                .body("name", equalTo("UniqueProduct"))
                .body("id", notNullValue());
    }

    @Test
    @RunOnVertxContext
    @Order(2)
    public void testDuplicateProductName(TransactionalUniAsserter asserter) {
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
                .post("/add")
                .then()
                .statusCode(201);

        // Second insert (same name)
        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(409)
                .body(equalTo("Product with the same name already exists"));
        asserter.execute( () -> Product.deleteAll() );
    }

    @Test
    @Transactional
    @TestTransaction
    public void testMissingName() {
        Product product = new Product();
        product.description = "Missing name test";
        product.price = 25.0;
        product.quantity = 1;

        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("/add")
                .then()
                .statusCode(400)
                .body(equalTo("Product name must not be empty"));
    }

    @Test
    @Transactional
    @TestTransaction
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
                .post("/add")
                .then()
                .statusCode(400)
                .body(equalTo("Price and quantity must be non-negative"));
    }
}
