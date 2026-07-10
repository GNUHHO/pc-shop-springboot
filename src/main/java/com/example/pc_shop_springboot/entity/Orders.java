package com.example.pc_shop_springboot.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Integer orderId;

    @Column(name = "customerid")
    private Integer customerId;

    // Bổ sung thêm cột EmployeeID từ thiết kế SQL ban đầu
    @Column(name = "employeeid")
    private Integer employeeId;

    @Column(name = "totalamount", nullable = false)
    private BigDecimal totalAmount;

    // Bổ sung thêm cột Tax (Thuế) từ thiết kế SQL ban đầu
    @Column(name = "tax")
    private BigDecimal tax;

    @Column(name = "status")
    private String status;

    @Column(name = "orderdate")
    private LocalDateTime orderDate;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

       public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}