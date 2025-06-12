package org.apibanking.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apibanking.entity.Product;
import org.apibanking.repository.ProductRepository;
import org.apibanking.resource.ProductResource;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository repo;
    private static final Logger log = Logger.getLogger(ProductResource.class);

    public Uni<List<Product>> fetchAll() {
        return repo.listAll();
    }

   public Uni<Response> create(Product product) {
        if (product.name == null || product.name.isBlank()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product name must not be empty").build());
        }
        if (product.price == null || product.price < 0 || product.quantity == null || product.quantity < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Price and quantity must be non-negative").build());
        }

        return repo.findByName(product.name)
                .onItem().ifNotNull().transform(existing -> Response.status(Response.Status.CONFLICT)
                        .entity("Product with the same name already exists").build())
                .onItem().ifNull().switchTo(() ->
                        Panache.withTransaction(() -> product.persist())
                                .map(p -> Response.status(Response.Status.CREATED).entity(p).build())
                                .onFailure().invoke(e -> log.error("Failed to persist product", e))
                );
    }

    public Uni<Response> getById(Long id) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product ID must be a positive number").build());
        }
        return repo.findById(id)
                .onItem().ifNotNull().transform(p -> Response.ok(p).build())
                .onItem().ifNull().continueWith(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity("Product not found for ID: " + id).build());
    }


    public Uni<Response> update(Long id, Product updatedProduct) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(400).entity("Invalid product ID").build());
        }
        return repo.findById(id)
                .onItem().ifNotNull().invoke(existing -> {
                    if (updatedProduct.name != null && !updatedProduct.name.isBlank()) {
                        existing.name = updatedProduct.name;
                    }
                    if (updatedProduct.description != null) {
                        existing.description = updatedProduct.description;
                    }
                    if (updatedProduct.price != null && updatedProduct.price >= 0) {
                        existing.price = updatedProduct.price;
                    }
                    if (updatedProduct.quantity != null && updatedProduct.quantity >= 0) {
                        existing.quantity = updatedProduct.quantity;
                    }
                })
                .onItem().ifNotNull().call(Product::flush)
                .onItem().ifNotNull().transform(p -> Response.ok(p).build())
                .onItem().ifNull().continueWith(() -> Response.status(404)
                        .entity("Product not found for ID: " + id).build());
    }

    public Uni<Response> delete(Long id) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product ID must be a positive number").build());
        }

        return Panache.withTransaction(() -> repo.deleteById(id))
                .map(deleted -> {
                    if (deleted) {
                        return Response.ok("Product deleted").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found for ID: " + id).build();
                    }
                })
                .onFailure().invoke(e -> log.error("Error deleting product", e));
    }

    public Uni<Response> checkStock(Long id, int count) {
        return repo.findById(id)
                .onItem().ifNotNull().transform(product -> {
                    boolean available = product.quantity >= count;
                    return Response.ok(available ? "Stock Available" : "Stock Not Available").build();
                })
                .onItem().ifNull().continueWith(() ->
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found for ID: " + id).build());
    }

    public Uni<List<Product>> fetchSortedByPrice() {
        return repo.getAllSortedByPrice();
    }
}
