package com.example.pc_shop_springboot.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Integer productId) {
        super("Product not found with id: " + productId);
    }
}
