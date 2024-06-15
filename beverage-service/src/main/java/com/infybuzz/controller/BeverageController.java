package com.infybuzz.controller;

import com.infybuzz.entity.Beverage;
import com.infybuzz.request.CreateBeverageRequest;
import com.infybuzz.service.BeverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beverage")
public class BeverageController {

    @Autowired
    BeverageService beverageService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Beverage>> getAllBeverages() {
        return ResponseEntity.ok(beverageService.getAllBeverages());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Beverage> getById(@PathVariable long id) {
        return ResponseEntity.ok(beverageService.getById(id));
    }

    @PostMapping("/create")
    public Beverage createBeverage(@RequestBody CreateBeverageRequest createAddressRequest) {
        return beverageService.createBeverage(createAddressRequest);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Beverage> updateBeverage(@PathVariable Long id, @RequestBody CreateBeverageRequest createBeverageRequest) {
        Beverage updatedBeverage = beverageService.updateBeverage(id, createBeverageRequest);
        return ResponseEntity.ok(updatedBeverage);
    }

    @PutMapping("/update-availability/{id}")
    public ResponseEntity<Void> updateBeverageAvailability(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        beverageService.updateBeverageAvailability(id, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBeverage(@PathVariable Long id) {
        beverageService.deleteBeverage(id);
        return ResponseEntity.noContent().build();
    }
}
