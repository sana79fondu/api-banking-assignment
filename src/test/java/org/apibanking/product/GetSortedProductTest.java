package org.apibanking.product;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apibanking.entity.Product;
import org.apibanking.resource.ProductResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestHTTPEndpoint(ProductResource.class)
public class GetSortedProductTest {

    @Test
    public void testSortedByPrice() {
        // Insert products with different prices
        Product p1 = new Product();
        p1.name = "Cheapest";
        p1.description = "Low price";
        p1.price = 10.0;
        p1.quantity = 1;

        Product p2 = new Product();
        p2.name = "MidRange";
        p2.description = "Medium price";
        p2.price = 50.0;
        p2.quantity = 2;

        Stream.of(p1, p2).forEach(product -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(product)
                    .when()
                    .post("/add")
                    .then()
                    .statusCode(201);
        });

        // Validate sorting
        List<String> names = given()
                .when()
                .get("/sorted-by-price")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("name");

        // Should be sorted from cheapest to most expensive
        assertThat(names).containsExactly("Cheapest", "MidRange");
    }

    @Test
    public void testSortedByPriceEmptyList() {
        // In case no products exist, this should return 200 + empty list
        given()
                .when()
                .get("/sorted-by-price")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}