package com.infybuzz.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class AuthAPIException extends RuntimeException {
    private HttpStatus status;

    public AuthAPIException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
