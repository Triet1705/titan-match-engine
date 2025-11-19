package com.titanmatch.core.engine;

import com.titanmatch.core.model.Order;
import com.titanmatch.core.model.Trade;
import com.titanmatch.core.model.enums.Side;

import java.math.BigDecimal;
import java.util.*;

public class OrderBook {
    private final TreeMap<BigDecimal, List<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<BigDecimal, List<Order>> asks = new TreeMap<>();
    private final List<Trade> generatedTrades = new ArrayList<>();

    public void addOrder(Order order) {
        matchOrder(order);

        if (order.getQuantity() > 0) {
            TreeMap<BigDecimal, List<Order>> bookSide = (order.getSide() == Side.BUY) ? bids : asks;

            bookSide.computeIfAbsent(order.getPrice(), k -> new LinkedList<>())
                    .add(order);
        }
    }

    private void matchOrder(Order incomingOrder) {
        TreeMap<BigDecimal, List<Order>> oppositeBook = (incomingOrder.getSide() == Side.BUY) ? asks : bids;
        Iterator<Map.Entry<BigDecimal, List<Order>>> iterator = oppositeBook.entrySet().iterator();
        while (iterator.hasNext() && incomingOrder.getQuantity() > 0) {
            Map.Entry<BigDecimal, List<Order>> entry = iterator.next();
            BigDecimal bestPrice = entry.getKey();
            List<Order> ordersAtPrice = entry.getValue();

            boolean canMatch = (incomingOrder.getSide() == Side.BUY && incomingOrder.getPrice().compareTo(bestPrice) >= 0) ||
                    (incomingOrder.getSide() == Side.SELL && incomingOrder.getPrice().compareTo(bestPrice) <= 0);

            if (!canMatch) {
                break;
            }

            Iterator<Order> orderIterator = ordersAtPrice.iterator();
            while (orderIterator.hasNext() && incomingOrder.getQuantity() > 0) {
                Order existingOrder = orderIterator.next();
                double tradeQty = Math.min(incomingOrder.getQuantity(), existingOrder.getQuantity());
                generatedTrades.add(new Trade(
                        (incomingOrder.getSide() == Side.BUY) ? incomingOrder.getOrderId() : existingOrder.getOrderId(),
                        (incomingOrder.getSide() == Side.SELL) ? incomingOrder.getOrderId() : existingOrder.getOrderId(),
                        bestPrice,
                        tradeQty,
                        System.currentTimeMillis()
                ));

                incomingOrder.setQuantity(incomingOrder.getQuantity() - tradeQty);
                existingOrder.setQuantity(existingOrder.getQuantity() - tradeQty);

                if (existingOrder.getQuantity() <= 0) {
                    orderIterator.remove();
                }
            }

            if (ordersAtPrice.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void printOrderBook() {
        System.out.println("--- ASKS (Sellers) ---");
        asks.forEach((price, orders) -> {
            double totalQty = orders.stream().mapToDouble(Order::getQuantity).sum();
            System.out.printf("Price: %s | Qty: %.2f | Orders: %d%n", price, totalQty, orders.size());
        });

        System.out.println("--- BIDS (Buyers) ---");
        bids.forEach((price, orders) -> {
            double totalQty = orders.stream().mapToDouble(Order::getQuantity).sum();
            System.out.printf("Price: %s | Qty: %.2f | Orders: %d%n", price, totalQty, orders.size());
        });
        System.out.println("----------------------");
    }

    public List<Trade> getGeneratedTrades() {
        return generatedTrades;
    }
}