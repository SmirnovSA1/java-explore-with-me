package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.Constant.FORMATTER;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ErrorResponse {
    private HttpStatus status;
    private String reason;
    private String message;
    private List<String> errors;
    private String timestamp = FORMATTER.format(LocalDateTime.now());
}
