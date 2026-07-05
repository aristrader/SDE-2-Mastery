package org.example.backend_fundamentals.java.foundations.exceptions.playground;

import java.io.IOException;

public class ExceptionHandlingPractice {

    public static class Account{
        private int balance;

        Account(int balance){
            if(balance < 0){
                throw new InvalidAccountStateException("Balance cannot be less than 0");
            }
            this.balance = balance;
        }

        public void withdraw(int amount) throws InsufficientFundsException {
            if(amount <= 0){
                throw new InvalidAccountStateException("Cannot withdraw negative amount");
            }
            if(amount>this.balance){
                throw new InsufficientFundsException("Insufficient funds", amount - this.balance);
            }
            this.balance = this.balance - amount;
            System.out.println("WITHDREW amount" + amount);
        }
    }

    // --- Topic 2: Exception translation ---

    private static String readFile(String filename) throws java.io.IOException {
        // simulate a low-level failure
        throw new java.io.IOException("File not found: " + filename);
    }

    public static void loadUserConfig(String filename) {
        try {
            readFile(filename);
        } catch (IOException e){
            throw new ConfigLoadException(e.getMessage(), e);
        }
    }

    // --- Topic 3: try-with-resources ---

    public static class Resource implements AutoCloseable {
        private final String name;
        private final boolean throwOnClose;

        Resource(String name) {
            this(name, false);
        }

        Resource(String name, boolean throwOnClose) {
            this.name = name;
            this.throwOnClose = throwOnClose;
            System.out.println("Opening Resource " + name);
        }

        public void use() {
            System.out.println("Using Resource " + name);
        }

        @Override
        public void close() {
            System.out.println("Closing Resource " + name);
            if (throwOnClose) {
                throw new RuntimeException("Close failed: " + name);
            }
        }
    }

    public static void main(String[] args) {

//        Account account1 = new Account(-100);

      Account account = new Account(100);
      try {
        account.withdraw(50);
      } catch (InsufficientFundsException e) {
        System.out.println("Message : " + e.getMessage() + ", shortfall of : " + e.getShortFall());
      }

        try {
            account.withdraw(1000);
        } catch (InsufficientFundsException e) {
            System.out.println("Message : " + e.getMessage() + ", shortfall of : " + e.getShortFall());
        }

        try {
            account.withdraw(-100);
        } catch (InsufficientFundsException e) {
            System.out.println("Message : " + e.getMessage() + ", shortfall of : " + e.getShortFall());
        } catch (InvalidAccountStateException e) {
            System.out.println("Invalid state: " + e.getMessage());
        }

        // Topic 2 — confirm cause is preserved through translation
        try {
            loadUserConfig("missing.cfg");
        } catch (ConfigLoadException e) {
            System.out.println("Cause type: " + e.getCause().getClass().getSimpleName());
        }

        // Topic 3a — close order is reverse of open order (r2 opens second, closes first)
        try (Resource r1 = new Resource("r1");
             Resource r2 = new Resource("r2")) {
            r1.use();
            r2.use();
        }

        System.out.println("---------------------------");

        // Topic 3b — body exception is primary; close exception is suppressed
        // Prediction: primary = "Body failed", suppressed[0] = "Close failed: r1"
        try (Resource r1 = new Resource("r1", true);
             Resource r2 = new Resource("r2")) {
            r1.use();
            throw new RuntimeException("Body failed");
        } catch (RuntimeException e) {
            System.out.println("Primary: " + e.getMessage());
            System.out.println("Suppressed: " + e.getSuppressed()[0].getMessage());
        }
    }
}
