package com.infybuzz.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
public class FallbackController {

    @RequestMapping("/orderServiceFallback")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String orderFallback() {
        return "Order Service is down!";
    }

    @RequestMapping("/beverageServiceFallback")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String beverageFallback() {
        return "Beverage Service is down!";
    }

    @RequestMapping("/authServiceFallback")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String authFallback() {
        return "Auth Service is down!";
    }
}

