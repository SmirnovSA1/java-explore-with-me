package ru.practicum.exception;

public class EndTimeBeforeStartTimeException extends RuntimeException {
    public EndTimeBeforeStartTimeException(final String message) {
        super(message);
    }
}
