package com.infybuzz.service;

import com.infybuzz.entity.Beverage;
import com.infybuzz.entity.BeverageType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BeverageSpecification {

    public static Specification<Beverage> search(
            String query,
            Long beverageId,
            String beverageName,
            Double beverageCost,
            BeverageType beverageType,
            Integer availability
    ) {
        return (Root<Beverage> root, CriteriaQuery<?> cq, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // General search
            if (query != null && !query.isEmpty()) {
                Predicate orPredicate = cb.disjunction();

                // Match beverage name
                orPredicate = cb.or(orPredicate, cb.like(cb.lower(root.get("beverageName")), "%" + query.toLowerCase() + "%"));

                // Match cost and availability if query is a number
                try {
                    Double cost = Double.parseDouble(query);
                    orPredicate = cb.or(orPredicate, cb.equal(root.get("beverageCost"), cost));
                } catch (NumberFormatException ignored) {}

                try {
                    Integer avail = Integer.parseInt(query);
                    orPredicate = cb.or(orPredicate, cb.equal(root.get("availability"), avail));
                } catch (NumberFormatException ignored) {}

                // Match beverage type
                try {
                    BeverageType type = BeverageType.valueOf(query.toUpperCase());
                    orPredicate = cb.or(orPredicate, cb.equal(root.get("beverageType"), type));
                } catch (IllegalArgumentException ignored) {}

                predicates.add(orPredicate);
            }

            // Filter by ID
            if (beverageId != null) {
                predicates.add(cb.equal(root.get("beverageId"), beverageId));
            }

            // Filter by name
            if (beverageName != null && !beverageName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("beverageName")), "%" + beverageName.toLowerCase() + "%"));
            }

            // Filter by cost
            if (beverageCost != null) {
                predicates.add(cb.equal(root.get("beverageCost"), beverageCost));
            }

            // Filter by type
            if (beverageType != null) {
                predicates.add(cb.equal(root.get("beverageType"), beverageType));
            }

            // Filter by availability
            if (availability != null) {
                predicates.add(cb.equal(root.get("availability"), availability));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
