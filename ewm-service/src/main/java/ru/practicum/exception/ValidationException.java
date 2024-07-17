package ru.practicum.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(final String message, final List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
