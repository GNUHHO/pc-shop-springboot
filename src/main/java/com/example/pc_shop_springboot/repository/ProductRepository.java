package com.example.pc_shop_springboot.repository;

import com.example.pc_shop_springboot.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsBySku(String sku);

    // Spring Data JPA automatically provides basic CRUD operations.
    // We can define custom queries here later (e.g., findBySku).
}
