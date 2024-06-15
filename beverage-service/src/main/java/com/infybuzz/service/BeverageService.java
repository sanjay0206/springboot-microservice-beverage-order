package com.infybuzz.service;

import com.infybuzz.entity.Beverage;
import com.infybuzz.repository.BeverageRepository;
import com.infybuzz.request.CreateBeverageRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BeverageService {
	Logger logger = LoggerFactory.getLogger(BeverageService.class);
	
	@Autowired
	BeverageRepository beverageRepository;

	public Beverage getById(long id) {
		logger.info("Inside getById " + id);
		return beverageRepository.findById(id).orElse(null);
	}

	public List<Beverage> getAllBeverages() {
		return beverageRepository.findAll();
	}

	public Beverage createBeverage(CreateBeverageRequest createBeverageRequest) {
		Beverage beverage = Beverage.builder()
				.beverageName(createBeverageRequest.getBeverageName())
				.beverageCost(createBeverageRequest.getBeverageCost())
				.beverageType(createBeverageRequest.getBeverageType())
				.availability(createBeverageRequest.getAvailability())
				.createdAt(LocalDateTime.now())
				.modifiedAt(null)
				.build();

        return beverageRepository.save(beverage);
	}

	@Transactional
	public void updateBeverageAvailability(long id, int quantity) {
		logger.info("Inside updateBeverageAvailability | beverageId = " + id + " quantity = " + quantity);
		Beverage existingBeverage = beverageRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Beverage not found"));

		int updatedAvailability = existingBeverage.getAvailability() - quantity;
		logger.info("existingAvailability = " + existingBeverage.getAvailability()  + " updatedAvailability = " + updatedAvailability);

		existingBeverage.setAvailability(updatedAvailability);
		existingBeverage.setModifiedAt(LocalDateTime.now());
	}

	@Transactional
	public Beverage updateBeverage(Long id, CreateBeverageRequest createBeverageRequest) {
		logger.info("Inside updateBeverage " + id + " " + createBeverageRequest);
		Beverage existingBeverage = beverageRepository.findById(id).orElseThrow(() -> new RuntimeException("Beverage not found"));

		if (createBeverageRequest.getBeverageName() != null) {
			existingBeverage.setBeverageName(createBeverageRequest.getBeverageName());
		}
		if (createBeverageRequest.getBeverageCost() != null) {
			existingBeverage.setBeverageCost(createBeverageRequest.getBeverageCost());
		}
		if (createBeverageRequest.getBeverageType() != null) {
			existingBeverage.setBeverageType(createBeverageRequest.getBeverageType());
		}
		if (createBeverageRequest.getAvailability() != null) {
			existingBeverage.setAvailability(createBeverageRequest.getAvailability());
		}
		existingBeverage.setModifiedAt(LocalDateTime.now());
		return existingBeverage;
	}

	public void deleteBeverage(Long id) {
		logger.info("Inside deleteRecipe " + id);
		if (!beverageRepository.existsById(id)) {
			throw new IllegalStateException("Beverage not found.");
		}
		beverageRepository.deleteById(id);
	}
}
