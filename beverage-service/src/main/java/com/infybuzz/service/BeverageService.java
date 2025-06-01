package com.infybuzz.service;

import com.infybuzz.entity.Beverage;
import com.infybuzz.entity.BeverageType;
import com.infybuzz.repository.BeverageRepository;
import com.infybuzz.request.CreateBeverageRequest;
import com.infybuzz.response.PagedBeverageResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class BeverageService {

	private final BeverageRepository beverageRepository;

	@Autowired
    public BeverageService(BeverageRepository beverageRepository) {
        this.beverageRepository = beverageRepository;
    }

    public Beverage getById(long id) {
        log.info("Inside getById {}", id);
		return beverageRepository.findById(id).orElse(null);
	}

	public PagedBeverageResponse getBeverages(int pageNo, int pageSize) {
		int pageIndex = Math.max(0, pageNo - 1);
		Pageable pageable = PageRequest.of(pageIndex, pageSize);
		Page<Beverage> beveragePage = beverageRepository.findAll(pageable);

		return new PagedBeverageResponse(
				beveragePage.getContent(),
				beveragePage.getNumber() + 1,
				beveragePage.getSize(),
				beveragePage.getTotalElements(),
				beveragePage.getTotalPages(),
				beveragePage.isLast()
		);
	}

	public PagedBeverageResponse searchBeverages(String query,
												 Long beverageId,
												 String beverageName,
												 Double beverageCost,
												 BeverageType beverageType,
												 Integer availability,
												 Pageable pageable) {
		Specification<Beverage> spec = BeverageSpecification.search(query, beverageId, beverageName, beverageCost, beverageType, availability);
		Page<Beverage> beveragePage = beverageRepository.findAll(spec, pageable);

		return new PagedBeverageResponse(
				beveragePage.getContent(),
				beveragePage.getNumber() + 1,
				beveragePage.getSize(),
				beveragePage.getTotalElements(),
				beveragePage.getTotalPages(),
				beveragePage.isLast()
		);
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
		log.info("Inside updateBeverageAvailability | beverageId = {} quantity = {}", id, quantity);
		Beverage existingBeverage = beverageRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Beverage not found"));

		int updatedAvailability = existingBeverage.getAvailability() - quantity;
		log.info("existingAvailability = {} updatedAvailability = {}",
				existingBeverage.getAvailability(), updatedAvailability);

		existingBeverage.setAvailability(updatedAvailability);
		existingBeverage.setModifiedAt(LocalDateTime.now());
	}

	@Transactional
	public Beverage updateBeverage(Long id, CreateBeverageRequest createBeverageRequest) {
		log.info("Inside updateBeverage {} {}", id, createBeverageRequest);
		Beverage existingBeverage = beverageRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Beverage not found"));

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
		log.info("Inside deleteRecipe {}", id);
		if (!beverageRepository.existsById(id)) {
			throw new IllegalStateException("Beverage not found.");
		}
		beverageRepository.deleteById(id);
	}
}
