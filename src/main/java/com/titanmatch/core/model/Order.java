package com.titanmatch.core.model;

import com.titanmatch.core.model.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long orderId;
    private Long userId;
    private BigDecimal price;
    private double quantity;
    private double originalQuantity;
    private Side side;
    private long timestamp;

    public Order(Long orderId, Long userId, BigDecimal price, double quantity, Side side, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.timestamp = timestamp;
    }
}
