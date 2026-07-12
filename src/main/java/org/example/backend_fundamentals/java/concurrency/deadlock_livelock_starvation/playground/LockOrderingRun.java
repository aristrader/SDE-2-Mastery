package org.example.backend_fundamentals.java.concurrency.deadlock_livelock_starvation.playground;

public class LockOrderingRun {
    public static void main(String[] args) throws InterruptedException {
        Account first = new Account(1, 100);
        Account second = new Account(2, 100);
        TransferService service = new TransferService();

        Thread a = new Thread(() -> service.transfer(first, second, 10));
        Thread b = new Thread(() -> service.transfer(second, first, 20));
        a.start();
        b.start();
        a.join();
        b.join();

        if (first.balance + second.balance != 200) {
            throw new AssertionError("money was created or lost");
        }
        System.out.println("balances=" + first.balance + "," + second.balance);
    }

    static final class Account {
        private final long id;
        private long balance;

        Account(long id, long balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    static final class TransferService {
        void transfer(Account source, Account destination, long amount) {
            Account first = source.id < destination.id ? source : destination;
            Account second = source.id < destination.id ? destination : source;

            synchronized (first) {
                synchronized (second) {
                    if (source.balance < amount) {
                        return;
                    }
                    source.balance -= amount;
                    destination.balance += amount;
                }
            }
        }
    }
}
