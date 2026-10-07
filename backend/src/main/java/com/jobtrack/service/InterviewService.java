package com.jobtrack.service;

import com.jobtrack.dto.request.InterviewCreateRequest;
import com.jobtrack.dto.request.InterviewUpdateRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.InterviewMapper;
import com.jobtrack.repository.ApplicationRepository;
import com.jobtrack.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public InterviewResponse createInterview(Long applicationId, InterviewCreateRequest request, User user) {
        Application application = resolveApplication(applicationId, user);

        Interview interview = interviewMapper.toEntity(request);
        interview.setApplication(application);
        interview.setStatus(InterviewStatus.SCHEDULED);

        Interview saved = interviewRepository.save(interview);
        log.info("Created interview ID {} for application ID {} by user ID {}",
                saved.getId(), applicationId, user.getId());
        return interviewMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(Long applicationId, User user) {
        resolveApplication(applicationId, user);
        List<Interview> interviews = interviewRepository.findAllByApplicationIdOrderByScheduledAtAsc(applicationId);
        return interviewMapper.toResponseList(interviews);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterview(Long applicationId, Long interviewId, User user) {
        resolveApplication(applicationId, user);
        Interview interview = resolveInterview(interviewId, applicationId);
        return interviewMapper.toResponse(interview);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public InterviewResponse updateInterview(Long applicationId, Long interviewId, InterviewUpdateRequest request, User user) {
        resolveApplication(applicationId, user);
        Interview interview = resolveInterview(interviewId, applicationId);

        interviewMapper.updateEntityFromRequest(request, interview);

        // Apply non-null fields from request explicitly for nullable fields
        if (request.getType() != null) {
            interview.setType(request.getType());
        }
        if (request.getScheduledAt() != null) {
            interview.setScheduledAt(request.getScheduledAt());
        }
        // endedAt, location, interviewerName, notes, feedback may be set to null intentionally
        interview.setEndedAt(request.getEndedAt());
        interview.setLocation(request.getLocation());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setNotes(request.getNotes());
        interview.setFeedback(request.getFeedback());

        Interview saved = interviewRepository.save(interview);
        log.info("Updated interview ID {} for application ID {} by user ID {}",
                interviewId, applicationId, user.getId());
        return interviewMapper.toResponse(saved);
    }

    @Transactional
    public InterviewResponse updateStatus(Long applicationId, Long interviewId, InterviewStatus newStatus, String notes, User user) {
        resolveApplication(applicationId, user);
        Interview interview = resolveInterview(interviewId, applicationId);

        validateStatusTransition(interview.getStatus(), newStatus);

        interview.setStatus(newStatus);
        if (notes != null && !notes.isBlank()) {
            interview.setNotes(interview.getNotes() != null
                    ? interview.getNotes() + "\n[Status → " + newStatus + "] " + notes
                    : "[Status → " + newStatus + "] " + notes);
        }

        Interview saved = interviewRepository.save(interview);
        log.info("Status of interview ID {} changed to {} by user ID {}", interviewId, newStatus, user.getId());
        return interviewMapper.toResponse(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteInterview(Long applicationId, Long interviewId, User user) {
        resolveApplication(applicationId, user);
        Interview interview = resolveInterview(interviewId, applicationId);
        interviewRepository.delete(interview);
        log.info("Deleted interview ID {} from application ID {} by user ID {}",
                interviewId, applicationId, user.getId());
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Verify that the application exists and belongs to the authenticated user.
     * Throws ResourceNotFoundException (→ 404) otherwise.
     */
    private Application resolveApplication(Long applicationId, User user) {
        return applicationRepository.findByIdAndUserId(applicationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));
    }

    /**
     * Verify that the interview exists and belongs to the specified application.
     * Throws ResourceNotFoundException (→ 404) otherwise.
     */
    private Interview resolveInterview(Long interviewId, Long applicationId) {
        return interviewRepository.findByIdAndApplicationId(interviewId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", interviewId));
    }

    /**
     * Only SCHEDULED interviews can transition to another status.
     * Completed, cancelled, and no-show interviews are terminal.
     */
    private void validateStatusTransition(InterviewStatus current, InterviewStatus next) {
        if (current != InterviewStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Cannot change status of an interview that is already " + current +
                    ". Only SCHEDULED interviews can have their status updated.");
        }
    }
}
