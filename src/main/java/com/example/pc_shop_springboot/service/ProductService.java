package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.dto.CreateProductRequest;
import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.exception.CategoryNotFoundException;
import com.example.pc_shop_springboot.exception.DuplicateProductSkuException;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.repository.CategoryRepository;
import com.example.pc_shop_springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateProductSkuException(request.getSku());
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new CategoryNotFoundException(request.getCategoryId());
        }

        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setCurrentDynamicPrice(request.getCurrentDynamicPrice());
        product.setSpecs(request.getSpecs());
        product.setIsActive(request.getIsActive() == null ? true : request.getIsActive());

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    private ProductResponse mapToProductResponse(Product product) {
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
