package com.example.pc_shop_springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ProductResponse {
    private Integer productId;
    private Integer categoryId;
    private String sku;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal currentDynamicPrice;
    private Map<String, Object> specs;
    private Boolean isActive;
}
