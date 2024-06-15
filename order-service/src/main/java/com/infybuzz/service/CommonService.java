package com.infybuzz.service;

import com.infybuzz.feignclients.BeverageFeignClient;
import com.infybuzz.response.BeverageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonService {
    Logger logger = LoggerFactory.getLogger(CommonService.class);

    long count = 1;

    @Autowired
    BeverageFeignClient beverageFeignClient;

    @CircuitBreaker(name = "beverageService", fallbackMethod = "fallbackGetBeverageById")
    public BeverageResponse getBeverageById(long beverageId) {
        logger.info("count = " + count);
        count++;

        return beverageFeignClient.getById(beverageId);
    }

    public BeverageResponse fallbackGetBeverageById(long beverageId, Throwable th) {
        logger.error("Error while fetching beverageId = " + beverageId + "\n " + th.getMessage());
        return new BeverageResponse();
    }

    @CircuitBreaker(name = "beverageService", fallbackMethod = "fallbackUpdateBeverageAvailability")
    public void updateBeverageAvailability(long beverageId, int quantity) {
        beverageFeignClient.updateBeverageAvailability(beverageId, quantity);
    }

    public void fallbackUpdateBeverageAvailability(long beverageId, int quantity, Throwable th) {
        logger.error("Error updating beverage availability for beverageId = " + beverageId + ", quantity = " + quantity + ", error = " + th.getMessage());
    }
}
