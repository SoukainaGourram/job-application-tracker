package com.jobtrack.dto.response;

import com.jobtrack.entity.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Dashboard statistics response aggregating data for the authenticated user.
 * Returned by GET /api/dashboard/stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    /** Total number of job applications for this user. */
    private long totalApplications;

    /** Total number of job offers saved by this user. */
    private long totalOffers;

    /** Total number of companies tracked by this user. */
    private long totalCompanies;

    /** Total number of interviews (all statuses) for this user. */
    private long totalInterviews;

    /** Number of applications per status (all 8 statuses of ApplicationStatus). */
    private Map<ApplicationStatus, Long> applicationsByStatus;

    /** The 5 most recently created applications (sorted by createdAt desc). */
    private List<ApplicationSummaryResponse> recentApplications;

    /** The next 5 upcoming SCHEDULED interviews sorted by scheduledAt asc. */
    private List<UpcomingInterviewDto> upcomingInterviews;
}
