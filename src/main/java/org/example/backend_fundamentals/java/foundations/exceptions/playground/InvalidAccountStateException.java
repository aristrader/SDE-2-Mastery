package org.example.backend_fundamentals.java.foundations.exceptions.playground;

public class InvalidAccountStateException extends RuntimeException {

    InvalidAccountStateException(String message) {
        super(message);
    }
}