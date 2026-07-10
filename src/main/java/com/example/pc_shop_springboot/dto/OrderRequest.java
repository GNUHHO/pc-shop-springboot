package com.example.pc_shop_springboot.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Integer customerId;
    private Integer productId;
    private Integer quantity;
}
