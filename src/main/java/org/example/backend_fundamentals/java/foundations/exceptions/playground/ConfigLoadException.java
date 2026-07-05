package org.example.backend_fundamentals.java.foundations.exceptions;

public class ConfigLoadException extends RuntimeException {

    ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
