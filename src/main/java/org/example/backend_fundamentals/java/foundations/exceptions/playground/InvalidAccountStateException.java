package org.example.backend_fundamentals.java.foundations.exceptions;

public class InvalidAccountStateException extends RuntimeException {

    InvalidAccountStateException(String message) {
        super(message);
    }
}