package com.example.pc_shop_springboot.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Integer categoryId) {
        super("Category not found with id: " + categoryId);
    }
}
