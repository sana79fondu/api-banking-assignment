package org.apibanking.resource;


import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apibanking.entity.Product;
import org.apibanking.service.ProductService;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ProductResource {

    @Inject
    private ProductService productService;
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
        return productService.fetchAll();
    }

    @POST
    @Path("/add")
    public Uni<Response> create(Product product) {
        log.info("Saving product: " + product.name);
        return productService.create(product);
    }


    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") Long id) {
       return productService.getById(id);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Product updatedProduct) {
        return productService.update(id,updatedProduct);
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return productService.delete(id);
    }

    @GET
    @Path("/{id}/stock")
    public Uni<Response> checkStock(@PathParam("id") Long id, @QueryParam("count") int count) {
        return productService.checkStock(id,count);
    }

    @GET
    @Path("/sorted-by-price")
    public Uni<List<Product>> sortedByPrice() {
        return productService.fetchSortedByPrice();
    }

}
