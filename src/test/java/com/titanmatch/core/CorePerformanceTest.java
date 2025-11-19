package com.titanmatch.core;

import com.titanmatch.core.engine.MatchingEngine;
import com.titanmatch.core.model.Order;
import com.titanmatch.core.model.enums.Side;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CorePerformanceTest {

    @Test
    public void testHighConcurrencyThroughput() throws InterruptedException {
        MatchingEngine engine = new MatchingEngine();
        int numberOfOrders = 1_000_000;
        int numberOfThreads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        Random rand = new Random();
        AtomicInteger orderIdGen = new AtomicInteger(0);

        System.out.println("🚀 START STRESS TEST: " + numberOfOrders + " orders...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < (numberOfOrders / numberOfThreads); j++) {
                        Side side = rand.nextBoolean() ? Side.BUY : Side.SELL;

                        BigDecimal price = BigDecimal.valueOf(100 + rand.nextInt(100));

                        double quantity = 1 + rand.nextInt(10);

                        Order order = new Order(
                                (long) orderIdGen.incrementAndGet(),
                                1L, // Mock User ID
                                price,
                                quantity,
                                side,
                                System.currentTimeMillis()
                        );

                        engine.processOrder(order);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        double throughput = (double) numberOfOrders / duration * 1000;

        System.out.println("✅ TEST COMPLETED!");
        System.out.println("⏱ Duration: " + duration + "ms");
        System.out.println("🔥 Throughput: " + String.format("%,.0f", throughput) + " orders/sec");

    }
}