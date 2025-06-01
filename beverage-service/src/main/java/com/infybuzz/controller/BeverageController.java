package com.infybuzz.controller;

import com.infybuzz.entity.Beverage;
import com.infybuzz.entity.BeverageType;
import com.infybuzz.request.CreateBeverageRequest;
import com.infybuzz.response.PagedBeverageResponse;
import com.infybuzz.service.BeverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beverage")
@Tag(name = "Beverage API", description = "Operations related to beverages")
public class BeverageController {

    private final BeverageService beverageService;

    @Autowired
    public BeverageController(BeverageService beverageService) {
        this.beverageService = beverageService;
    }

    @Operation(summary = "Get paginated list of beverages", description = "Accessible by USER role")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedBeverageResponse> getBeverages(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return new ResponseEntity<>(beverageService.getBeverages(pageNo, pageSize), HttpStatus.OK);
    }

    @Operation(summary = "Search beverages with filters", description = "Accessible by USER role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedBeverageResponse> searchBeverages(
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @Parameter(description = "Beverage ID") @RequestParam(required = false) Long beverageId,
            @Parameter(description = "Beverage name") @RequestParam(required = false) String beverageName,
            @Parameter(description = "Beverage cost") @RequestParam(required = false) Double beverageCost,
            @Parameter(description = "Beverage type") @RequestParam(required = false) BeverageType beverageType,
            @Parameter(description = "Availability count") @RequestParam(required = false) Integer availability,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size);
        return ResponseEntity.ok(beverageService.searchBeverages(query, beverageId, beverageName, beverageCost, beverageType, availability, pageable));
    }

    @Operation(summary = "Get beverage by ID", description = "Accessible by USER role")
    @ApiResponse(responseCode = "200", description = "Beverage found")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Beverage> getById(@PathVariable long id) {
        return ResponseEntity.ok(beverageService.getById(id));
    }

    @Operation(summary = "Update beverage availability", description = "Accessible by USER role")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update-availability/{id}")
    public ResponseEntity<Void> updateBeverageAvailability(
            @PathVariable("id") Long id,
            @RequestParam("quantity") int quantity) {
        beverageService.updateBeverageAvailability(id, quantity);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a new beverage", description = "Accessible by SHOP_OWNER role")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/create")
    public Beverage createBeverage(@RequestBody CreateBeverageRequest createAddressRequest) {
        return beverageService.createBeverage(createAddressRequest);
    }

    @Operation(summary = "Update an existing beverage", description = "Accessible by SHOP_OWNER role")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<Beverage> updateBeverage(
            @PathVariable Long id,
            @RequestBody CreateBeverageRequest createBeverageRequest) {
        Beverage updatedBeverage = beverageService.updateBeverage(id, createBeverageRequest);
        return ResponseEntity.ok(updatedBeverage);
    }

    @Operation(summary = "Delete a beverage", description = "Accessible by SHOP_OWNER role")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBeverage(@PathVariable Long id) {
        beverageService.deleteBeverage(id);
        return ResponseEntity.noContent().build();
    }
}
