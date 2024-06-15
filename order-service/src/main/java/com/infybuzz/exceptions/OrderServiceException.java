package com.infybuzz.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class OrderServiceException extends IllegalStateException {
    private HttpStatus status;
    private String message;
}
