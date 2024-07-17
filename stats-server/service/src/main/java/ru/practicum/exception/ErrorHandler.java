package ru.practicum.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static ru.practicum.Constant.REASON_BAD_REQUEST;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleEndTimeBeforeStartTimeException(final EndTimeBeforeStartTimeException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason(REASON_BAD_REQUEST)
                .message(e.getMessage())
                .errors(e.getErrors())
                .build();
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), errorResponse.getStatus());
    }
}
