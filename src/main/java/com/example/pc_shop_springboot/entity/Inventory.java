package com.example.pc_shop_springboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventoryid")
    private Integer inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productid", referencedColumnName = "productid")
    private Product product;

    @Column(name = "supplierid")
    private Integer supplierId;

    @Column(name = "quantityonhand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "costperunit", nullable = false)
    private BigDecimal costPerUnit;

    @Column(name = "location")
    private String location;

    @Column(name = "lastrestocked")
    private LocalDateTime lastRestocked;
}