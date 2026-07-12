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
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @Mock
    private ProductPriceHistoryRepository productPriceHistoryRepository;

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
    void updateProduct_whenRequestIsValid_updatesExistingEntityAndReturnsResponse() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1200.00"), new BigDecimal("1300.00"), "Annual update");
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndProductIdNot("PC-UPDATED", 1)).thenReturn(false);
        when(categoryRepository.existsById(3)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(1, request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertSame(existingProduct, capturedProduct);
        assertAll(
                () -> assertEquals(1, capturedProduct.getProductId()),
                () -> assertEquals(3, capturedProduct.getCategoryId()),
                () -> assertEquals("PC-UPDATED", capturedProduct.getSku()),
                () -> assertEquals("Updated Gaming PC", capturedProduct.getName()),
                () -> assertEquals(new BigDecimal("1200.00"), capturedProduct.getBasePrice()),
                () -> assertEquals(new BigDecimal("1300.00"), capturedProduct.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "64GB"), capturedProduct.getSpecs()),
                () -> assertEquals(false, capturedProduct.getIsActive()),
                () -> assertEquals(1, response.getProductId()),
                () -> assertEquals(3, response.getCategoryId()),
                () -> assertEquals("PC-UPDATED", response.getSku()),
                () -> assertEquals("Updated Gaming PC", response.getName()),
                () -> assertEquals(new BigDecimal("1200.00"), response.getBasePrice()),
                () -> assertEquals(new BigDecimal("1300.00"), response.getCurrentDynamicPrice()),
                () -> assertEquals(Map.of("ram", "64GB"), response.getSpecs()),
                () -> assertEquals(false, response.getIsActive())
        );
    }

    @Test
    void updateProduct_whenPricesDoNotChange_doesNotCreateHistory() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        stubValidUpdate(existingProduct);

        productService.updateProduct(1, request);

        verify(productRepository, times(1)).save(existingProduct);
        verify(productPriceHistoryRepository, never()).save(any(ProductPriceHistory.class));
    }

    @Test
    void updateProduct_whenPriceScaleOnlyChanges_doesNotCreateHistory() {
        Product existingProduct = createProduct();
        existingProduct.setBasePrice(new BigDecimal("1000.0"));
        existingProduct.setCurrentDynamicPrice(new BigDecimal("1100.0"));
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        stubValidUpdate(existingProduct);

        productService.updateProduct(1, request);

        verify(productPriceHistoryRepository, never()).save(any(ProductPriceHistory.class));
    }

    @Test
    void updateProduct_whenOnlyBasePriceChanges_createsBasePriceHistory() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1200.00"), new BigDecimal("1100.00"), "Base price review");
        stubValidUpdate(existingProduct);

        productService.updateProduct(1, request);

        ProductPriceHistory history = captureSavedHistory();
        assertAll(
                () -> assertEquals(1, history.getProductId()),
                () -> assertEquals(new BigDecimal("1000.00"), history.getOldBasePrice()),
                () -> assertEquals(new BigDecimal("1200.00"), history.getNewBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), history.getOldDynamicPrice()),
                () -> assertEquals(new BigDecimal("1100.00"), history.getNewDynamicPrice()),
                () -> assertEquals(PriceChangeType.BASE_PRICE, history.getChangeType()),
                () -> assertEquals(PriceChangeSource.MANUAL, history.getChangeSource()),
                () -> assertEquals("Base price review", history.getChangeReason()),
                () -> assertNotNull(history.getChangedAt())
        );
    }

    @Test
    void updateProduct_whenOnlyDynamicPriceChanges_createsDynamicPriceHistory() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1250.00"), "Dynamic price review");
        stubValidUpdate(existingProduct);

        productService.updateProduct(1, request);

        ProductPriceHistory history = captureSavedHistory();
        assertAll(
                () -> assertEquals(new BigDecimal("1000.00"), history.getOldBasePrice()),
                () -> assertEquals(new BigDecimal("1000.00"), history.getNewBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), history.getOldDynamicPrice()),
                () -> assertEquals(new BigDecimal("1250.00"), history.getNewDynamicPrice()),
                () -> assertEquals(PriceChangeType.CURRENT_DYNAMIC_PRICE, history.getChangeType()),
                () -> assertEquals(PriceChangeSource.MANUAL, history.getChangeSource())
        );
    }

    @Test
    void updateProduct_whenBothPricesChange_createsOneHistoryRecord() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1200.00"), new BigDecimal("1300.00"), "Both prices reviewed");
        stubValidUpdate(existingProduct);

        productService.updateProduct(1, request);

        ProductPriceHistory history = captureSavedHistory();
        assertAll(
                () -> assertEquals(new BigDecimal("1000.00"), history.getOldBasePrice()),
                () -> assertEquals(new BigDecimal("1200.00"), history.getNewBasePrice()),
                () -> assertEquals(new BigDecimal("1100.00"), history.getOldDynamicPrice()),
                () -> assertEquals(new BigDecimal("1300.00"), history.getNewDynamicPrice()),
                () -> assertEquals(PriceChangeType.BOTH, history.getChangeType()),
                () -> assertEquals(PriceChangeSource.MANUAL, history.getChangeSource())
        );
        verify(productPriceHistoryRepository, times(1)).save(any(ProductPriceHistory.class));
    }

    @Test
    void updateProduct_whenSkuIsUnchanged_updatesSuccessfully() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        request.setSku("PC-001");
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndProductIdNot("PC-001", 1)).thenReturn(false);
        when(categoryRepository.existsById(3)).thenReturn(true);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        productService.updateProduct(1, request);

        verify(productRepository).existsBySkuAndProductIdNot("PC-001", 1);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void updateProduct_whenProductDoesNotExist_throwsProductNotFound() {
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(99, request));

        verify(productRepository, never()).existsBySkuAndProductIdNot(any(String.class), any(Integer.class));
        verifyNoInteractions(categoryRepository);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(productPriceHistoryRepository);
    }

    @Test
    void updateProduct_whenSkuBelongsToAnotherProduct_throwsDuplicateSku() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndProductIdNot("PC-UPDATED", 1)).thenReturn(true);

        assertThrows(DuplicateProductSkuException.class, () -> productService.updateProduct(1, request));

        verifyNoInteractions(categoryRepository);
        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(productPriceHistoryRepository);
    }

    @Test
    void updateProduct_whenCategoryDoesNotExist_throwsCategoryNotFound() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1000.00"), new BigDecimal("1100.00"), null);
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndProductIdNot("PC-UPDATED", 1)).thenReturn(false);
        when(categoryRepository.existsById(3)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> productService.updateProduct(1, request));

        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(productPriceHistoryRepository);
    }

    @Test
    void updateProduct_whenHistorySaveFails_propagatesException() {
        Product existingProduct = createProduct();
        UpdateProductRequest request = createUpdateProductRequest(
                new BigDecimal("1200.00"), new BigDecimal("1100.00"), "Base price review");
        stubValidUpdate(existingProduct);
        RuntimeException historyFailure = new RuntimeException("History persistence failed");
        when(productPriceHistoryRepository.save(any(ProductPriceHistory.class))).thenThrow(historyFailure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> productService.updateProduct(1, request)
        );

        assertSame(historyFailure, thrown);
        verify(productRepository, times(1)).save(existingProduct);
        verify(productPriceHistoryRepository, times(1)).save(any(ProductPriceHistory.class));
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

    private UpdateProductRequest createUpdateProductRequest(
            BigDecimal basePrice,
            BigDecimal currentDynamicPrice,
            String changeReason
    ) {
        return new UpdateProductRequest(
                3,
                "PC-UPDATED",
                "Updated Gaming PC",
                basePrice,
                currentDynamicPrice,
                Map.of("ram", "64GB"),
                false,
                changeReason
        );
    }

    private void stubValidUpdate(Product existingProduct) {
        when(productRepository.findById(1)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsBySkuAndProductIdNot("PC-UPDATED", 1)).thenReturn(false);
        when(categoryRepository.existsById(3)).thenReturn(true);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
    }

    private ProductPriceHistory captureSavedHistory() {
        ArgumentCaptor<ProductPriceHistory> historyCaptor = ArgumentCaptor.forClass(ProductPriceHistory.class);
        verify(productPriceHistoryRepository, times(1)).save(historyCaptor.capture());
        return historyCaptor.getValue();
    }
}
