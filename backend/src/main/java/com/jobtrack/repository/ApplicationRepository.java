package com.jobtrack.repository;

import com.jobtrack.entity.Application;
import com.jobtrack.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByOfferIdAndUserIdAndStatusNotIn(Long offerId, Long userId, Collection<ApplicationStatus> statuses);

    List<Application> findAllByUserId(Long userId);

    List<Application> findAllByUserIdAndStatus(Long userId, ApplicationStatus status);

    Page<Application> findAllByUserId(Long userId, Pageable pageable);

    void deleteByIdAndUserId(Long id, Long userId);
}
