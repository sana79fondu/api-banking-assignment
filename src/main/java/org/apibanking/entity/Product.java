package org.apibanking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;

@Entity
public class Product extends PanacheEntity{

    public String name;
    @Column(length = 1000)
    public String description;
    public Double price;
    public Integer quantity;
}
