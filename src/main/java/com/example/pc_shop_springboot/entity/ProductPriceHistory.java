package com.example.pc_shop_springboot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_price_history")
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pricehistoryid")
    private Integer priceHistoryId;

    @Column(name = "productid", nullable = false)
    private Integer productId;

    @Column(name = "oldbaseprice", nullable = false, precision = 12, scale = 2)
    private BigDecimal oldBasePrice;

    @Column(name = "newbaseprice", nullable = false, precision = 12, scale = 2)
    private BigDecimal newBasePrice;

    @Column(name = "olddynamicprice", nullable = false, precision = 12, scale = 2)
    private BigDecimal oldDynamicPrice;

    @Column(name = "newdynamicprice", nullable = false, precision = 12, scale = 2)
    private BigDecimal newDynamicPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "changetype", nullable = false, length = 30)
    private PriceChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "changesource", nullable = false, length = 30)
    private PriceChangeSource changeSource;

    @Column(name = "changereason", length = 500)
    private String changeReason;

    @Column(name = "changedat", nullable = false)
    private OffsetDateTime changedAt;
}
