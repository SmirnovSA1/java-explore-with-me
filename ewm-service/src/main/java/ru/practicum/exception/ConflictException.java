package ru.practicum.exception;

import java.util.List;

public class ConflictException extends RuntimeException {
    private final List<String> errors;

    public ConflictException(final String message, final List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
