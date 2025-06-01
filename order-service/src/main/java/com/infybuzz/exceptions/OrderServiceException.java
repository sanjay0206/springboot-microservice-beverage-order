package com.infybuzz.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class OrderServiceException extends RuntimeException {
    private HttpStatus status;

    public OrderServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
