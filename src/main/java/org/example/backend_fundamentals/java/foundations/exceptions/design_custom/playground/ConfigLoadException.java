package org.example.backend_fundamentals.java.foundations.exceptions.design_custom.playground;

public class ConfigLoadException extends RuntimeException {

    ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
