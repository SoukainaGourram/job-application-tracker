package com.jobtrack.repository;

import com.jobtrack.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {

    List<CompanyContact> findAllByCompanyIdAndCompanyUserIdOrderByCreatedAtDesc(Long companyId, Long userId);

    Optional<CompanyContact> findByIdAndCompanyIdAndCompanyUserId(Long id, Long companyId, Long userId);

    void deleteByIdAndCompanyIdAndCompanyUserId(Long id, Long companyId, Long userId);

    long countByCompanyId(Long companyId);
}
