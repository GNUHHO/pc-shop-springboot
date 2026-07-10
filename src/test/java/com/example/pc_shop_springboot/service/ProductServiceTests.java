package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductById_whenProductExists_returnsProductResponse() {
        Product product = createProduct();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1);

        assertAll(
                () -> assertEquals(1, response.getProductId()),
                () -> assertEquals(2, response.getCategoryId()),
                () -> assertEquals("PC-001", response.getSku()),
                () -> assertEquals("Gaming PC", response.getName()),
                () -> assertEquals(new BigDecimal("1000.00"), response.getBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), response.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "32GB"), response.getSpecs()),
                () -> assertEquals(true, response.getIsActive())
        );
        verify(productRepository).findById(1);
    }

    @Test
    void getProductById_whenProductDoesNotExist_throwsProductNotFoundException() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(99));

        verify(productRepository).findById(99);
    }

    private Product createProduct() {
        Product product = new Product();
        product.setProductId(1);
        product.setCategoryId(2);
        product.setSku("PC-001");
        product.setName("Gaming PC");
        product.setBasePrice(new BigDecimal("1000.00"));
        product.setCurrentDynamicPrice(new BigDecimal("1100.00"));
        product.setSpecs(Map.of("ram", "32GB"));
        product.setIsActive(true);
        return product;
    }
}
