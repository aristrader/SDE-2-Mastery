package org.example.backend_fundamentals.java.foundations.exceptions.design_custom.playground;

public class InvalidAccountStateException extends RuntimeException {

    InvalidAccountStateException(String message) {
        super(message);
    }
}
