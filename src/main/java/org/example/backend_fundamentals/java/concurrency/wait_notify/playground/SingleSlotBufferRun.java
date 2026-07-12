package org.example.backend_fundamentals.java.concurrency.wait_notify.playground;

public class SingleSlotBufferRun {
    public static void main(String[] args) throws InterruptedException {
        SingleSlotBuffer<Integer> buffer = new SingleSlotBuffer<>();

        Thread producer = new Thread(() -> {
            try {
                buffer.put(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                int value = buffer.take();
                if (value != 10) {
                    throw new AssertionError("wrong value");
                }
                System.out.println("consumed=" + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        producer.start();
        producer.join();
        consumer.join();
    }

    static final class SingleSlotBuffer<T> {
        private T value;
        private boolean available;

        synchronized void put(T newValue) throws InterruptedException {
            while (available) {
                wait();
            }
            value = newValue;
            available = true;
            notifyAll();
        }

        synchronized T take() throws InterruptedException {
            while (!available) {
                wait();
            }
            T result = value;
            value = null;
            available = false;
            notifyAll();
            return result;
        }
    }
}
