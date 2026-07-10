package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return new ProductResponse(
                product.getProductId(),
                product.getCategoryId(),
                product.getSku(),
                product.getName(),
                product.getBasePrice(),
                product.getCurrentDynamicPrice(),
                product.getSpecs(),
                product.getIsActive()
        );
    }
}
