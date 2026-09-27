package com.jobtrack.dto.response;

import com.jobtrack.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private OfferResponse offer;
    private ApplicationStatus status;
    private LocalDate appliedAt;
    private String notes;
    private String coverLetter;
    private String cvVersion;
    private String source;
    private Instant createdAt;
    private Instant updatedAt;
}
