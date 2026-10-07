package com.jobtrack.repository;

import com.jobtrack.entity.Interview;
import com.jobtrack.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /**
     * Find all interviews for an application, sorted most recent first.
     * IDOR is handled at service level by first verifying application ownership.
     */
    List<Interview> findAllByApplicationIdOrderByScheduledAtAsc(Long applicationId);

    /**
     * Find interview by id AND application id — prevents cross-application access.
     */
    Optional<Interview> findByIdAndApplicationId(Long id, Long applicationId);

    /**
     * Used during Application deletion to cascade-delete interviews.
     */
    void deleteAllByApplicationId(Long applicationId);

    long countByApplicationId(Long applicationId);

    /**
     * Finds upcoming SCHEDULED interviews for a given user (via Application → User).
     * Used by the dashboard to display next interviews across all applications.
     */
    List<Interview> findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
            Long userId, InterviewStatus status, Instant scheduledAtAfter);

    /**
     * Counts all interviews for a given user (via Application → User).
     */
    long countByApplication_UserId(Long userId);
}
