package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.dto.CreateProductRequest;
import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.exception.CategoryNotFoundException;
import com.example.pc_shop_springboot.exception.DuplicateProductSkuException;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.repository.CategoryRepository;
import com.example.pc_shop_springboot.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_whenRequestIsValid_savesAndReturnsResponse() {
        CreateProductRequest request = createProductRequest(true);
        Product savedProduct = createProduct();
        savedProduct.setProductId(10);
        when(productRepository.existsBySku("PC-001")).thenReturn(false);
        when(categoryRepository.existsById(2)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).existsBySku("PC-001");
        verify(productRepository).save(productCaptor.capture());
        Product productToSave = productCaptor.getValue();
        assertAll(
                () -> assertNull(productToSave.getProductId()),
                () -> assertEquals(2, productToSave.getCategoryId()),
                () -> assertEquals("PC-001", productToSave.getSku()),
                () -> assertEquals("Gaming PC", productToSave.getName()),
                () -> assertEquals(new BigDecimal("1000.00"), productToSave.getBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), productToSave.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "32GB"), productToSave.getSpecs()),
                () -> assertEquals(true, productToSave.getIsActive()),
                () -> assertEquals(10, response.getProductId()),
                () -> assertEquals(2, response.getCategoryId()),
                () -> assertEquals("PC-001", response.getSku()),
                () -> assertEquals("Gaming PC", response.getName()),
                () -> assertEquals(new BigDecimal("1000.00"), response.getBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), response.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "32GB"), response.getSpecs()),
                () -> assertEquals(true, response.getIsActive())
        );
    }

    @Test
    void createProduct_whenIsActiveIsNull_defaultsToTrue() {
        CreateProductRequest request = createProductRequest(null);
        when(productRepository.existsBySku("PC-001")).thenReturn(false);
        when(categoryRepository.existsById(2)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.createProduct(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(true, productCaptor.getValue().getIsActive());
    }

    @Test
    void createProduct_whenIsActiveIsFalse_preservesFalse() {
        CreateProductRequest request = createProductRequest(false);
        when(productRepository.existsBySku("PC-001")).thenReturn(false);
        when(categoryRepository.existsById(2)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.createProduct(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(false, productCaptor.getValue().getIsActive());
    }

    @Test
    void createProduct_whenSkuExists_throwsDuplicateProductSkuException() {
        CreateProductRequest request = createProductRequest(true);
        when(productRepository.existsBySku("PC-001")).thenReturn(true);

        DuplicateProductSkuException exception = assertThrows(
                DuplicateProductSkuException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Product with SKU already exists: PC-001", exception.getMessage());
        verify(productRepository).existsBySku("PC-001");
        verifyNoInteractions(categoryRepository);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_whenCategoryDoesNotExist_throwsCategoryNotFoundExceptionAndDoesNotSave() {
        CreateProductRequest request = createProductRequest(true);
        when(productRepository.existsBySku("PC-001")).thenReturn(false);
        when(categoryRepository.existsById(2)).thenReturn(false);

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Category not found with id: 2", exception.getMessage());
        verify(productRepository, times(1)).existsBySku("PC-001");
        verify(categoryRepository, times(1)).existsById(2);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_whenProductsExist_returnsProductResponses() {
        Product firstProduct = createProduct();
        Product secondProduct = createSecondProduct();
        when(productRepository.findAll()).thenReturn(List.of(firstProduct, secondProduct));

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(2, responses.size());
        ProductResponse firstResponse = responses.get(0);
        ProductResponse secondResponse = responses.get(1);
        assertAll(
                () -> assertEquals(1, firstResponse.getProductId()),
                () -> assertEquals(2, firstResponse.getCategoryId()),
                () -> assertEquals("PC-001", firstResponse.getSku()),
                () -> assertEquals("Gaming PC", firstResponse.getName()),
                () -> assertEquals(new BigDecimal("1000.00"), firstResponse.getBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), firstResponse.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "32GB"), firstResponse.getSpecs()),
                () -> assertEquals(true, firstResponse.getIsActive()),
                () -> assertEquals(2, secondResponse.getProductId()),
                () -> assertEquals(3, secondResponse.getCategoryId()),
                () -> assertEquals("PC-002", secondResponse.getSku()),
                () -> assertEquals("Office PC", secondResponse.getName()),
                () -> assertEquals(new BigDecimal("700.00"), secondResponse.getBasePrice()),
                () -> assertEquals(new BigDecimal("750.00"), secondResponse.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "16GB"), secondResponse.getSpecs()),
                () -> assertEquals(false, secondResponse.getIsActive())
        );
        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_whenNoProductsExist_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponse> responses = productService.getAllProducts();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(productRepository).findAll();
    }

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

    private Product createSecondProduct() {
        Product product = new Product();
        product.setProductId(2);
        product.setCategoryId(3);
        product.setSku("PC-002");
        product.setName("Office PC");
        product.setBasePrice(new BigDecimal("700.00"));
        product.setCurrentDynamicPrice(new BigDecimal("750.00"));
        product.setSpecs(Map.of("ram", "16GB"));
        product.setIsActive(false);
        return product;
    }

    private CreateProductRequest createProductRequest(Boolean isActive) {
        return new CreateProductRequest(
                2,
                "PC-001",
                "Gaming PC",
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00"),
                Map.of("ram", "32GB"),
                isActive
        );
    }
}
