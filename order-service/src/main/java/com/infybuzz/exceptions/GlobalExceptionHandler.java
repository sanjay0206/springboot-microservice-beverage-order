package com.infybuzz.exceptions;

import com.infybuzz.response.ErrorDetailsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // handle resource specific exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetailsResponse> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                                WebRequest webRequest) {
        ErrorDetailsResponse errorDetails = ErrorDetailsResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(webRequest.getDescription(false))
                .build();

        log.info("ErrorDetailsResponse: " + errorDetails);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // handle Order API exceptions
    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<ErrorDetailsResponse> handleRecipeAPIException(OrderServiceException exception,
                                                                         WebRequest webRequest) {
        ErrorDetailsResponse errorDetails = ErrorDetailsResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(webRequest.getDescription(false))
                .build();

        log.info("ErrorDetailsResponse: " + errorDetails);
        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // handle global exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetailsResponse> handleGlobalException(Exception exception,
                                                                      WebRequest webRequest) {
        ErrorDetailsResponse errorDetails = ErrorDetailsResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(webRequest.getDescription(false))
                .build();

        log.info("ErrorDetailsResponse: " + errorDetails);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
