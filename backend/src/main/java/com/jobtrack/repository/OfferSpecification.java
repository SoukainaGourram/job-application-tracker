package com.jobtrack.repository;

import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.Offer;
import com.jobtrack.entity.OfferStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OfferSpecification {

    public static Specification<Offer> withFilters(
            Long userId,
            String search,
            OfferStatus status,
            ContractType contractType,
            String city,
            String country,
            String technology) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory security filter: always restrict to the current user
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // ContractType filter
            if (contractType != null) {
                predicates.add(cb.equal(root.get("contractType"), contractType));
            }

            // City filter
            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.trim().toLowerCase() + "%"));
            }

            // Country filter
            if (country != null && !country.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("country")), "%" + country.trim().toLowerCase() + "%"));
            }

            // Global search in title, companyName, description
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate companyPredicate = cb.like(cb.lower(root.get("companyName")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titlePredicate, companyPredicate, descPredicate));
            }

            // Filter by specific technology
            if (technology != null && !technology.isBlank()) {
                Join<Offer, String> techJoin = root.join("technologies");
                predicates.add(cb.like(cb.lower(techJoin), "%" + technology.trim().toLowerCase() + "%"));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
