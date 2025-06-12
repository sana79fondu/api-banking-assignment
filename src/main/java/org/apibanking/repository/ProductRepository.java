package org.apibanking.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apibanking.entity.Product;

import java.util.List;

@ApplicationScoped
public class ProductRepository  implements PanacheRepository<Product> {

    public Uni<List<Product>> getAllSortedByPrice() {
        return list("ORDER BY price ASC");
    }

    public Uni<Product> findByName(String name) {
        return find("name", name).firstResult();
    }

}
