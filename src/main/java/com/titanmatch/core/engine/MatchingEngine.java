package com.titanmatch.core.engine;

import com.titanmatch.core.model.Order;
import lombok.Getter;

@Getter
public class MatchingEngine {

    private final OrderBook orderBook;

    public MatchingEngine() {
        this.orderBook = new OrderBook();
    }

    public synchronized void processOrder(Order order) {
        orderBook.addOrder(order);
    }
}