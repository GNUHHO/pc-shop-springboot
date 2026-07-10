package com.example.pc_shop_springboot.entity; // Adjust package name to yours

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productid")
    private Integer productId;

    @Column(name = "categoryid")
    private Integer categoryId;

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "baseprice", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "currentdynamicprice", nullable = false)
    private BigDecimal currentDynamicPrice;

    // THE MAGIC: Automatically map PostgreSQL JSONB to a Java Map
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specs", columnDefinition = "jsonb")
    private Map<String, Object> specs;

    @Column(name = "isactive")
    private Boolean isActive;
}