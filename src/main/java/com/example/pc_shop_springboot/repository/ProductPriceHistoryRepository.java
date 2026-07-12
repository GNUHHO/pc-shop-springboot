package com.example.pc_shop_springboot.repository;

import com.example.pc_shop_springboot.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Integer> {
}
