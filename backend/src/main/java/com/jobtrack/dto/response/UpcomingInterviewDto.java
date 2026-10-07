package com.jobtrack.dto.response;

import com.jobtrack.entity.InterviewStatus;
import com.jobtrack.entity.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Lightweight DTO for upcoming interviews on the dashboard.
 * Enriches the interview with its application context (offer title, company name)
 * since Interview → Application → Offer → Company.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingInterviewDto {

    private Long id;
    private Long applicationId;
    private String offerTitle;
    private String companyName;
    private InterviewType type;
    private InterviewStatus status;
    private Instant scheduledAt;
    private String location;
}
