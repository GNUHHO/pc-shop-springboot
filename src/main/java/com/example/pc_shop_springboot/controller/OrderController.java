package com.example.pc_shop_springboot.controller;

import com.example.pc_shop_springboot.dto.OrderRequest;
import com.example.pc_shop_springboot.entity.Orders;
import com.example.pc_shop_springboot.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestBody OrderRequest request) {
        Orders newOrder = orderService.placeOrder(
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity()

        );
        return ResponseEntity.ok(newOrder);
    }
}