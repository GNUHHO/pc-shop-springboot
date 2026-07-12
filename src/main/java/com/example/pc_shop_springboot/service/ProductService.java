package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.dto.CreateProductRequest;
import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.dto.UpdateProductRequest;
import com.example.pc_shop_springboot.entity.PriceChangeSource;
import com.example.pc_shop_springboot.entity.PriceChangeType;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.entity.ProductPriceHistory;
import com.example.pc_shop_springboot.exception.CategoryNotFoundException;
import com.example.pc_shop_springboot.exception.DuplicateProductSkuException;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.repository.CategoryRepository;
import com.example.pc_shop_springboot.repository.ProductRepository;
import com.example.pc_shop_springboot.repository.ProductPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductPriceHistoryRepository productPriceHistoryRepository;

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

    @Transactional
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (productRepository.existsBySkuAndProductIdNot(request.getSku(), productId)) {
            throw new DuplicateProductSkuException(request.getSku());
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new CategoryNotFoundException(request.getCategoryId());
        }

        BigDecimal oldBasePrice = existingProduct.getBasePrice();
        BigDecimal oldDynamicPrice = existingProduct.getCurrentDynamicPrice();
        boolean basePriceChanged = oldBasePrice.compareTo(request.getBasePrice()) != 0;
        boolean dynamicPriceChanged = oldDynamicPrice.compareTo(request.getCurrentDynamicPrice()) != 0;

        existingProduct.setCategoryId(request.getCategoryId());
        existingProduct.setSku(request.getSku());
        existingProduct.setName(request.getName());
        existingProduct.setBasePrice(request.getBasePrice());
        existingProduct.setCurrentDynamicPrice(request.getCurrentDynamicPrice());
        existingProduct.setSpecs(request.getSpecs());
        existingProduct.setIsActive(request.getIsActive());

        Product savedProduct = productRepository.save(existingProduct);

        if (basePriceChanged || dynamicPriceChanged) {
            ProductPriceHistory history = new ProductPriceHistory();
            history.setProductId(existingProduct.getProductId());
            history.setOldBasePrice(oldBasePrice);
            history.setNewBasePrice(request.getBasePrice());
            history.setOldDynamicPrice(oldDynamicPrice);
            history.setNewDynamicPrice(request.getCurrentDynamicPrice());
            history.setChangeType(determinePriceChangeType(basePriceChanged, dynamicPriceChanged));
            history.setChangeSource(PriceChangeSource.MANUAL);
            history.setChangeReason(request.getChangeReason());
            history.setChangedAt(OffsetDateTime.now());
            productPriceHistoryRepository.save(history);
        }

        return mapToProductResponse(savedProduct);
    }

    private PriceChangeType determinePriceChangeType(boolean basePriceChanged, boolean dynamicPriceChanged) {
        if (basePriceChanged && dynamicPriceChanged) {
            return PriceChangeType.BOTH;
        }
        return basePriceChanged
                ? PriceChangeType.BASE_PRICE
                : PriceChangeType.CURRENT_DYNAMIC_PRICE;
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
