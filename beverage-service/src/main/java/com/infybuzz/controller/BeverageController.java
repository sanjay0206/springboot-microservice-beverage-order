package com.infybuzz.controller;

import com.infybuzz.entity.Beverage;
import com.infybuzz.request.CreateBeverageRequest;
import com.infybuzz.service.BeverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beverage")
public class BeverageController {

    @Autowired
    BeverageService beverageService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<Beverage>> getAllBeverages(@RequestHeader(value = "X-Internal-Request", required = false)
                                                              String requestHeader) {
        return ResponseEntity.ok(beverageService.getAllBeverages());
    }

    @PreAuthorize("hasRole('USER') or #requestHeader == 'Internal'")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Beverage> getById(@PathVariable long id,
                                            @RequestHeader(value = "X-Internal-Request", required = false) String requestHeader) {
        return ResponseEntity.ok(beverageService.getById(id));
    }

    @PreAuthorize("hasRole('SHOP_OWNER') or #requestHeader == 'Internal'")
    @PutMapping("/update-availability/{id}")
    public ResponseEntity<Void> updateBeverageAvailability(@PathVariable("id") Long id,
                                                           @RequestParam("quantity") int quantity,
                                                           @RequestHeader(value = "X-Internal-Request", required = false) String requestHeader) {
        beverageService.updateBeverageAvailability(id, quantity);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/create")
    public Beverage createBeverage(@RequestBody CreateBeverageRequest createAddressRequest) {
        return beverageService.createBeverage(createAddressRequest);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<Beverage> updateBeverage(@PathVariable Long id,
                                                   @RequestBody CreateBeverageRequest createBeverageRequest) {
        Beverage updatedBeverage = beverageService.updateBeverage(id, createBeverageRequest);
        return ResponseEntity.ok(updatedBeverage);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBeverage(@PathVariable Long id) {
        beverageService.deleteBeverage(id);
        return ResponseEntity.noContent().build();
    }
}
