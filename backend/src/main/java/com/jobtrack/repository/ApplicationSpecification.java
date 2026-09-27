package com.jobtrack.repository;

import com.jobtrack.entity.Application;
import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.Offer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> withFilters(
            Long userId,
            String search,
            ApplicationStatus status,
            String companyName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory security filter: restrict to current user
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            Join<Application, Offer> offerJoin = root.join("offer");

            // Company filter
            if (companyName != null && !companyName.isBlank()) {
                predicates.add(cb.like(cb.lower(offerJoin.get("companyName")), "%" + companyName.trim().toLowerCase() + "%"));
            }

            // Global search in offer title or company name or notes
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(offerJoin.get("title")), pattern);
                Predicate companyPred = cb.like(cb.lower(offerJoin.get("companyName")), pattern);
                Predicate notesPred = cb.like(cb.lower(root.get("notes")), pattern);
                predicates.add(cb.or(titlePred, companyPred, notesPred));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
