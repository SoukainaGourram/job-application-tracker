package com.jobtrack.service;

import com.jobtrack.dto.request.ApplicationCreateRequest;
import com.jobtrack.dto.request.ApplicationUpdateRequest;
import com.jobtrack.dto.response.ApplicationHistoryResponse;
import com.jobtrack.dto.response.ApplicationResponse;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.entity.*;
import com.jobtrack.exception.ApplicationConflictException;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.mapper.ApplicationHistoryMapper;
import com.jobtrack.mapper.ApplicationMapper;
import com.jobtrack.repository.ApplicationHistoryRepository;
import com.jobtrack.repository.ApplicationRepository;
import com.jobtrack.repository.ApplicationSpecification;
import com.jobtrack.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private static final List<ApplicationStatus> INACTIVE_STATUSES = List.of(
            ApplicationStatus.REJECTED,
            ApplicationStatus.WITHDRAWN
    );

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final OfferRepository offerRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationHistoryMapper applicationHistoryMapper;

    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> getApplications(
            User user,
            String search,
            ApplicationStatus status,
            String companyName,
            Pageable pageable) {

        Specification<Application> spec = ApplicationSpecification.withFilters(
                user.getId(),
                search,
                status,
                companyName
        );

        return applicationRepository.findAll(spec, pageable)
                .map(applicationMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<ApplicationSummaryResponse> getKanbanApplications(User user) {
        return applicationRepository.findAllByUserId(user.getId()).stream()
                .map(applicationMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id, User user) {
        Application application = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        return applicationMapper.toResponse(application);
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request, User user) {
        Offer offer = offerRepository.findByIdAndUserId(request.getOfferId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", request.getOfferId()));

        checkNoActiveApplication(offer.getId(), user.getId());

        Application application = applicationMapper.toEntity(request);
        application.setUser(user);
        application.setOffer(offer);

        if (application.getStatus() == null) {
            application.setStatus(ApplicationStatus.TO_APPLY);
        }

        if (application.getStatus() == ApplicationStatus.APPLIED && application.getAppliedAt() == null) {
            application.setAppliedAt(LocalDate.now());
        }

        Application saved = applicationRepository.save(application);

        // Record initial history
        createHistoryEntry(saved, null, saved.getStatus(), "Candidature initialisée");

        log.info("Created application ID {} for offer ID {} and user ID {}", saved.getId(), offer.getId(), user.getId());
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public ApplicationResponse createApplicationFromOffer(Long offerId, User user) {
        Offer offer = offerRepository.findByIdAndUserId(offerId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", offerId));

        checkNoActiveApplication(offer.getId(), user.getId());

        Application application = Application.builder()
                .user(user)
                .offer(offer)
                .status(ApplicationStatus.TO_APPLY)
                .source(offer.getSource())
                .build();

        Application saved = applicationRepository.save(application);

        // Record initial history
        createHistoryEntry(saved, null, ApplicationStatus.TO_APPLY, "Candidature créée depuis l'offre");

        log.info("Created application ID {} directly from offer ID {} for user ID {}", saved.getId(), offerId, user.getId());
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(Long id, ApplicationUpdateRequest request, User user) {
        Application application = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));

        applicationMapper.updateEntityFromRequest(request, application);
        Application updated = applicationRepository.save(application);

        log.info("Updated application ID {} for user ID {}", updated.getId(), user.getId());
        return applicationMapper.toResponse(updated);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, String note, User user) {
        Application application = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));

        ApplicationStatus oldStatus = application.getStatus();
        if (oldStatus != newStatus) {
            application.setStatus(newStatus);

            if (newStatus == ApplicationStatus.APPLIED && application.getAppliedAt() == null) {
                application.setAppliedAt(LocalDate.now());
            }

            Application saved = applicationRepository.save(application);

            String historyNote = (note != null && !note.isBlank())
                    ? note
                    : "Statut modifié de " + oldStatus + " à " + newStatus;

            createHistoryEntry(saved, oldStatus, newStatus, historyNote);

            log.info("Status of application ID {} changed from {} to {} for user ID {}", id, oldStatus, newStatus, user.getId());
            return applicationMapper.toResponse(saved);
        }

        return applicationMapper.toResponse(application);
    }

    @Transactional
    public void deleteApplication(Long id, User user) {
        Application application = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));

        // Delete associated history first
        List<ApplicationHistory> histories = applicationHistoryRepository
                .findAllByApplicationIdOrderByChangedAtDesc(id);
        applicationHistoryRepository.deleteAll(histories);

        applicationRepository.delete(application);
        log.info("Deleted application ID {} and its history for user ID {}", id, user.getId());
    }

    @Transactional(readOnly = true)
    public List<ApplicationHistoryResponse> getApplicationHistory(Long id, User user) {
        // Ensure user owns application
        if (!applicationRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ResourceNotFoundException("Application", id);
        }

        return applicationHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(id).stream()
                .map(applicationHistoryMapper::toResponse)
                .toList();
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private void checkNoActiveApplication(Long offerId, Long userId) {
        boolean exists = applicationRepository.existsByOfferIdAndUserIdAndStatusNotIn(
                offerId,
                userId,
                INACTIVE_STATUSES
        );
        if (exists) {
            throw new ApplicationConflictException(offerId);
        }
    }

    private void createHistoryEntry(Application application, ApplicationStatus oldStatus, ApplicationStatus newStatus, String note) {
        ApplicationHistory history = ApplicationHistory.builder()
                .application(application)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(Instant.now())
                .note(note)
                .build();
        applicationHistoryRepository.save(history);
    }
}
