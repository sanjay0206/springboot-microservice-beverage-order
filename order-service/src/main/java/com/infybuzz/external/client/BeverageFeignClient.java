package com.infybuzz.external.client;

import com.infybuzz.exceptions.OrderServiceException;
import com.infybuzz.response.BeverageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        contextId = "beverage",
        value = "api-gateway",
        path = "/beverage-service/api/beverage",
        configuration = FeignClientConfig.class
)
public interface BeverageFeignClient {

    @CircuitBreaker(name = "external", fallbackMethod = "getByIdFallback")
    @GetMapping(path = "/{id}")
    BeverageResponse getById(@PathVariable long id);

    @CircuitBreaker(name = "external", fallbackMethod = "updateBeverageAvailabilityFallback")
    @PutMapping("/update-availability/{id}")
    void updateBeverageAvailability(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    // Fallback for getById
    default BeverageResponse getByIdFallback(long id, Throwable t) {
        throw new OrderServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Beverage service is unavailable");
    }

    // Fallback for updateBeverageAvailability
    default void updateBeverageAvailabilityFallback(Long id, int quantity, Throwable t) {
        throw new OrderServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Beverage update service is unavailable");
    }
}
