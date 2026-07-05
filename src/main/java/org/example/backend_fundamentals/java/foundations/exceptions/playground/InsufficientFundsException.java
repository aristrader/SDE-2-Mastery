package org.example.backend_fundamentals.java.foundations.exceptions.playground;

import lombok.Getter;

public class InsufficientFundsException extends Exception {

    @Getter
    private final double shortFall;

    InsufficientFundsException(String message, double shortFall) {
        super(message);
        this.shortFall = shortFall;
    }
}
