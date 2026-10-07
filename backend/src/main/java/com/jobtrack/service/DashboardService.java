package com.jobtrack.service;

import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.dto.response.UpcomingInterviewDto;
import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.Interview;
import com.jobtrack.entity.InterviewStatus;
import com.jobtrack.entity.User;
import com.jobtrack.mapper.ApplicationMapper;
import com.jobtrack.repository.ApplicationRepository;
import com.jobtrack.repository.CompanyRepository;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private static final int RECENT_APPLICATIONS_LIMIT = 5;
    private static final int UPCOMING_INTERVIEWS_LIMIT = 5;

    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final CompanyRepository companyRepository;
    private final InterviewRepository interviewRepository;
    private final ApplicationMapper applicationMapper;

    /**
     * Aggregates all dashboard statistics for the authenticated user.
     * All data is strictly scoped to the given user — no cross-user data leakage.
     *
     * @param user the authenticated user
     * @return a complete DashboardStatsResponse
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(User user) {
        Long userId = user.getId();
        log.info("Fetching dashboard stats for user ID {}", userId);

        // ── Totals ────────────────────────────────────────────────────────────
        long totalApplications = applicationRepository.countByUserId(userId);
        long totalOffers       = offerRepository.countByUserId(userId);
        long totalCompanies    = companyRepository.countByUserId(userId);
        long totalInterviews   = interviewRepository.countByApplication_UserId(userId);

        // ── Applications by status ────────────────────────────────────────────
        // Build a complete map with all ApplicationStatus values (0 if none exist)
        Map<ApplicationStatus, Long> applicationsByStatus = Arrays.stream(ApplicationStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> applicationRepository.countByUserIdAndStatus(userId, status)
                ));

        // ── Recent applications (5 most recent) ──────────────────────────────
        List<ApplicationSummaryResponse> recentApplications = applicationRepository
                .findAllByUserId(
                        userId,
                        PageRequest.of(0, RECENT_APPLICATIONS_LIMIT, Sort.by("createdAt").descending())
                )
                .map(applicationMapper::toSummaryResponse)
                .getContent();

        // ── Upcoming interviews (5 next SCHEDULED, from now onwards) ─────────
        List<UpcomingInterviewDto> upcomingInterviews = interviewRepository
                .findAllByApplication_UserIdAndStatusAndScheduledAtAfterOrderByScheduledAtAsc(
                        userId,
                        InterviewStatus.SCHEDULED,
                        Instant.now()
                )
                .stream()
                .limit(UPCOMING_INTERVIEWS_LIMIT)
                .map(this::toUpcomingInterviewDto)
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalApplications(totalApplications)
                .totalOffers(totalOffers)
                .totalCompanies(totalCompanies)
                .totalInterviews(totalInterviews)
                .applicationsByStatus(applicationsByStatus)
                .recentApplications(recentApplications)
                .upcomingInterviews(upcomingInterviews)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Maps an Interview entity to UpcomingInterviewDto, enriching it with
     * the offer title and company name from the associated application.
     */
    private UpcomingInterviewDto toUpcomingInterviewDto(Interview interview) {
        String offerTitle   = interview.getApplication().getOffer().getTitle();
        String companyName  = interview.getApplication().getOffer().getCompanyName();

        return UpcomingInterviewDto.builder()
                .id(interview.getId())
                .applicationId(interview.getApplication().getId())
                .offerTitle(offerTitle)
                .companyName(companyName)
                .type(interview.getType())
                .status(interview.getStatus())
                .scheduledAt(interview.getScheduledAt())
                .location(interview.getLocation())
                .build();
    }
}
