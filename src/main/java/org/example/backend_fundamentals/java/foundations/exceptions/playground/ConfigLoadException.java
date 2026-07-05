package org.example.backend_fundamentals.java.foundations.exceptions.playground;

public class ConfigLoadException extends RuntimeException {

    ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
