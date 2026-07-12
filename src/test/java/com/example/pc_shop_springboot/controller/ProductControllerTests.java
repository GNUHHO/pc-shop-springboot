package com.example.pc_shop_springboot.controller;

import com.example.pc_shop_springboot.dto.CreateProductRequest;
import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.exception.CategoryNotFoundException;
import com.example.pc_shop_springboot.exception.DuplicateProductSkuException;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_whenRequestIsValid_returnsCreatedAndProductJson() throws Exception {
        ProductResponse response = new ProductResponse(
                10,
                2,
                "PC-001",
                "Gaming PC",
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00"),
                Map.of("ram", "32GB"),
                true
        );
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateProductJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "/api/v1/products/10"))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.sku").value("PC-001"))
                .andExpect(jsonPath("$.name").value("Gaming PC"))
                .andExpect(jsonPath("$.basePrice").value(1000.00))
                .andExpect(jsonPath("$.currentDynamicPrice").value(1100.00))
                .andExpect(jsonPath("$.specs.ram").value("32GB"))
                .andExpect(jsonPath("$.isActive").value(true));

        ArgumentCaptor<CreateProductRequest> requestCaptor = ArgumentCaptor.forClass(CreateProductRequest.class);
        verify(productService).createProduct(requestCaptor.capture());
        CreateProductRequest request = requestCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals(2, request.getCategoryId()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("PC-001", request.getSku()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("Gaming PC", request.getName()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("1000.00"), request.getBasePrice()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("1100.00"), request.getCurrentDynamicPrice()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(Map.of("ram", "32GB"), request.getSpecs()),
                () -> org.junit.jupiter.api.Assertions.assertEquals(true, request.getIsActive())
        );
    }

    @Test
    void createProduct_whenSkuExists_returnsConflict() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new DuplicateProductSkuException("PC-001"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateProductJson()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product with SKU already exists: PC-001"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void createProduct_whenCategoryDoesNotExist_returnsNotFound() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new CategoryNotFoundException(999));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateProductJson()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void createProduct_whenCategoryIdIsNull_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("null", "PC-001", "Gaming PC", "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenCategoryIdIsNotPositive_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("0", "PC-001", "Gaming PC", "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenSkuIsBlank_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", " ", "Gaming PC", "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenSkuIsTooLong_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "A".repeat(51), "Gaming PC", "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenNameIsBlank_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", " ", "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenNameIsTooLong_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "A".repeat(201), "1000.00", "1100.00"));
    }

    @Test
    void createProduct_whenBasePriceIsNull_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "Gaming PC", "null", "1100.00"));
    }

    @Test
    void createProduct_whenBasePriceIsNegative_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "Gaming PC", "-1.00", "1100.00"));
    }

    @Test
    void createProduct_whenCurrentDynamicPriceIsNull_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "Gaming PC", "1000.00", "null"));
    }

    @Test
    void createProduct_whenCurrentDynamicPriceIsNegative_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "Gaming PC", "1000.00", "-1.00"));
    }

    @Test
    void createProduct_whenPriceHasMoreThanTwoDecimalPlaces_returnsBadRequest() throws Exception {
        assertCreateProductBadRequest(createProductJson("2", "PC-001", "Gaming PC", "1000.001", "1100.00"));
    }

    @Test
    void getAllProducts_whenProductsExist_returnsOkAndProductArray() throws Exception {
        ProductResponse firstProduct = new ProductResponse(
                1,
                2,
                "PC-001",
                "Gaming PC",
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00"),
                Map.of("ram", "32GB"),
                true
        );
        ProductResponse secondProduct = new ProductResponse(
                2,
                3,
                "PC-002",
                "Office PC",
                new BigDecimal("700.00"),
                new BigDecimal("750.00"),
                Map.of("ram", "16GB"),
                false
        );
        when(productService.getAllProducts()).thenReturn(List.of(firstProduct, secondProduct));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(2))
                .andExpect(jsonPath("$[0].sku").value("PC-001"))
                .andExpect(jsonPath("$[0].name").value("Gaming PC"))
                .andExpect(jsonPath("$[0].basePrice").value(1000.00))
                .andExpect(jsonPath("$[0].currentDynamicPrice").value(1100.00))
                .andExpect(jsonPath("$[0].specs.ram").value("32GB"))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].categoryId").value(3))
                .andExpect(jsonPath("$[1].sku").value("PC-002"))
                .andExpect(jsonPath("$[1].name").value("Office PC"))
                .andExpect(jsonPath("$[1].basePrice").value(700.00))
                .andExpect(jsonPath("$[1].currentDynamicPrice").value(750.00))
                .andExpect(jsonPath("$[1].specs.ram").value("16GB"))
                .andExpect(jsonPath("$[1].isActive").value(false));

        verify(productService).getAllProducts();
    }

    @Test
    void getAllProducts_whenNoProductsExist_returnsOkAndEmptyArray() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductById_whenProductExists_returnsOkAndProductJson() throws Exception {
        ProductResponse response = new ProductResponse(
                1,
                2,
                "PC-001",
                "Gaming PC",
                new BigDecimal("1000.00"),
                new BigDecimal("1100.00"),
                Map.of("ram", "32GB"),
                true
        );
        when(productService.getProductById(1)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.sku").value("PC-001"))
                .andExpect(jsonPath("$.name").value("Gaming PC"))
                .andExpect(jsonPath("$.basePrice").value(1000.00))
                .andExpect(jsonPath("$.currentDynamicPrice").value(1100.00))
                .andExpect(jsonPath("$.specs.ram").value("32GB"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getProductById_whenProductDoesNotExist_returnsNotFound() throws Exception {
        when(productService.getProductById(99)).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/99"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void getProductById_whenProductIdIsZero_returnsBadRequestWithoutCallingService() throws Exception {
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("productId must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/0"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verifyNoInteractions(productService);
    }

    @Test
    void getProductById_whenProductIdIsNegative_returnsBadRequestWithoutCallingService() throws Exception {
        mockMvc.perform(get("/api/v1/products/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("productId must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/-1"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verifyNoInteractions(productService);
    }

    private void assertCreateProductBadRequest(String requestBody) throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verifyNoInteractions(productService);
    }

    private String validCreateProductJson() {
        return createProductJson("2", "PC-001", "Gaming PC", "1000.00", "1100.00");
    }

    private String createProductJson(
            String categoryId,
            String sku,
            String name,
            String basePrice,
            String currentDynamicPrice
    ) {
        return """
                {
                  "categoryId": %s,
                  "sku": "%s",
                  "name": "%s",
                  "basePrice": %s,
                  "currentDynamicPrice": %s,
                  "specs": {"ram": "32GB"},
                  "isActive": true
                }
                """.formatted(categoryId, sku, name, basePrice, currentDynamicPrice);
    }
}
