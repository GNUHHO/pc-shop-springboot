package com.example.pc_shop_springboot.exception;

public class DuplicateProductSkuException extends RuntimeException {
    public DuplicateProductSkuException(String sku) {
        super("Product with SKU already exists: " + sku);
    }
}
