package com.jobtrack.repository;

import com.jobtrack.entity.Company;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CompanySpecification {

    public static Specification<Company> withFilters(
            Long userId,
            String search,
            String industry,
            String location,
            String size) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory security filter: restrict to current user
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Industry filter
            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("industry")), "%" + industry.trim().toLowerCase() + "%"));
            }

            // Location filter
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }

            // Size filter
            if (size != null && !size.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("size")), size.trim().toLowerCase()));
            }

            // Global search in name, industry, location, description
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate industryPredicate = cb.like(cb.lower(root.get("industry")), searchPattern);
                Predicate locationPredicate = cb.like(cb.lower(root.get("location")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(namePredicate, industryPredicate, locationPredicate, descPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
