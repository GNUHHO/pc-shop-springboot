package com.example.pc_shop_springboot.repository;

import com.example.pc_shop_springboot.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    // Spring Data JPA handles all standard CRUD operations automatically
}
