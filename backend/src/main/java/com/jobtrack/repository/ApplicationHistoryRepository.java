package com.jobtrack.repository;

import com.jobtrack.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

    List<ApplicationHistory> findAllByApplicationIdOrderByChangedAtDesc(Long applicationId);

    List<ApplicationHistory> findAllByApplicationIdAndApplicationUserIdOrderByChangedAtDesc(Long applicationId, Long userId);
}
