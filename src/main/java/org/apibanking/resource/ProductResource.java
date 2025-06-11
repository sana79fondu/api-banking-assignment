package org.apibanking.resource;


import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apibanking.entity.Product;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final Logger log = Logger.getLogger(ProductResource.class);


    @GET
    @Path("/healthcheck")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> checkHealth() {
        return Uni.createFrom().item(Response.ok("Healthy").build());
    }

    @GET
    @Path("/fetch-all")
    public Uni<List<Product>> getAll() {
        return Product.listAll();
    }


    @POST
    @Path("/add")
    public Uni<Response> create(Product product) {
        log.info("Saving product: " + product.name);

        if (product.name == null || product.name.isBlank()) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Product name must not be empty")
                            .build());
        }
        if (product.price == null || product.price < 0 || product.quantity == null || product.quantity < 0) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Price and quantity must be non-negative")
                            .build());
        }

        return Product.find("name", product.name).firstResult()
                .onItem().ifNotNull().transform(existing -> {
                    log.warn("Product with name already exists: " + product.name);
                    return Response.status(Response.Status.CONFLICT)
                            .entity("Product with the same name already exists")
                            .build();
                })
                .onItem().ifNull().switchTo(() ->
                        Panache.withTransaction(() -> product.persist())
                                .map(p -> Response.status(Response.Status.CREATED).entity(p).build())
                                .onFailure().invoke(e -> log.error("Failed to persist product", e))
                );
    }


    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") Long id) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product ID must be a positive number")
                    .build());
        }
        return Product.<Product>findById(id)
                .onItem().ifNotNull().transform(p -> {
                    return Response.ok(p).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Product not found for ID: " + id)
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Product updated) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(400).entity("Invalid product ID").build());
        }

        return Product.<Product>findById(id)
                .onItem().ifNotNull().invoke(existing -> {
                    if (updated.name != null && !updated.name.isBlank()) {
                        existing.name = updated.name;
                    }
                    if (updated.description != null) {
                        existing.description = updated.description;
                    }
                    if (updated.price != null && updated.price >= 0) {
                        existing.price = updated.price;
                    }
                    if (updated.quantity != null && updated.quantity >= 0) {
                        existing.quantity = updated.quantity;
                    }
                })
                .onItem().ifNotNull().call(Product::flush)
                .onItem().ifNotNull().transform(p -> Response.ok(p).build())
                .onItem().ifNull().continueWith(() -> Response.status(404).entity("Product not found for ID: " + id).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product ID must be a positive number")
                    .build());
        }

        return Panache.withTransaction(() -> Product.deleteById(id))
                .map(deleted -> {
                    if (deleted) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found for ID: " + id)
                                .build();
                    }
                })
                .onFailure().invoke(e -> log.error("Error deleting product", e));
    }

    @GET
    @Path("/{id}/stock")
    public Uni<Response> checkStock(@PathParam("id") Long id, @QueryParam("count") int count) {
        return Product.<Product>findById(id)
                .onItem().ifNotNull().transform(product -> {
                    boolean available = product.quantity >= count;
                    return Response.ok(available? "Stock Available" : "Stock Not available").build();
                })
                .onItem().ifNull().continueWith(() ->
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found for ID: " + id).build());
    }

    @GET
    @Path("/sorted-by-price")
    public Uni<List<Product>> sortedByPrice() {
        return Product.list("ORDER BY price ASC");
    }

}
