package com.jobtrack.repository;

import com.jobtrack.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    Optional<Offer> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    Page<Offer> findAllByUserId(Long userId, Pageable pageable);

    void deleteByIdAndUserId(Long id, Long userId);

    long countByCompanyId(Long companyId);

    List<Offer> findAllByCompanyId(Long companyId);

    @Modifying
    @Query("UPDATE Offer o SET o.company = NULL WHERE o.company.id = :companyId")
    void clearCompanyReference(@Param("companyId") Long companyId);

    long countByUserId(Long userId);
}
