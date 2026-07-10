package com.example.pc_shop_springboot.controller;

import com.example.pc_shop_springboot.dto.ProductResponse;
import com.example.pc_shop_springboot.exception.ProductNotFoundException;
import com.example.pc_shop_springboot.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
