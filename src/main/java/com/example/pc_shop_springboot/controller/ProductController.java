package com.example.pc_shop_springboot.controller;

import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.service.ProductService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable @Positive(message = "productId must be greater than 0") Integer productId
    ) {
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
}
