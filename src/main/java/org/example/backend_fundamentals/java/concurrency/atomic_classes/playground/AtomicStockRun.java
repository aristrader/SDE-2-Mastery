package org.example.backend_fundamentals.java.concurrency.atomic_classes.playground;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicStockRun {
    public static void main(String[] args) throws InterruptedException {
        Inventory inventory = new Inventory(1);

        Thread first = new Thread(() -> System.out.println("first=" + inventory.purchase()));
        Thread second = new Thread(() -> System.out.println("second=" + inventory.purchase()));
        first.start();
        second.start();
        first.join();
        second.join();

        if (inventory.remaining() != 0) {
            throw new AssertionError("stock should end at zero");
        }
    }

    static final class Inventory {
        private final AtomicInteger stock;

        Inventory(int stock) {
            this.stock = new AtomicInteger(stock);
        }

        boolean purchase() {
            while (true) {
                int current = stock.get();
                if (current <= 0) {
                    return false;
                }
                if (stock.compareAndSet(current, current - 1)) {
                    return true;
                }
            }
        }

        int remaining() {
            return stock.get();
        }
    }
}
