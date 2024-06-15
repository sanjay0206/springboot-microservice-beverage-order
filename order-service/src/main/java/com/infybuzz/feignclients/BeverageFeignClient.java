package com.infybuzz.feignclients;

import com.infybuzz.response.BeverageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "api-gateway", path = "/beverage-service/api/beverage", configuration = FeignClientConfig.class)
public interface BeverageFeignClient {
    @GetMapping(path = "/getById/{id}")
    BeverageResponse getById(@PathVariable long id);

    @PutMapping("/update-availability/{id}")
    void updateBeverageAvailability(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

}
