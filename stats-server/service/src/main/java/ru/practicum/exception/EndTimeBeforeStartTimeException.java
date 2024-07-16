package ru.practicum.exception;

import java.util.List;

public class EndTimeBeforeStartTimeException extends RuntimeException {
    private final List<String> errors;

    public EndTimeBeforeStartTimeException(final String message, final List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
