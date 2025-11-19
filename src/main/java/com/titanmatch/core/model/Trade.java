package com.titanmatch.core.model;

import com.titanmatch.core.model.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Trade {
    private Long buyOrderId;
    private Long sellOrderId;
    private BigDecimal price;
    private double quantity;
    private long timestamp;
}
