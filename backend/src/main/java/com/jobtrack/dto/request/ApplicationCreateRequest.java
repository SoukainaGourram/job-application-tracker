package com.jobtrack.dto.request;

import com.jobtrack.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateRequest {

    @NotNull(message = "Offer ID is required")
    private Long offerId;

    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.TO_APPLY;

    private LocalDate appliedAt;

    private String notes;

    private String coverLetter;

    @Size(max = 100, message = "CV version cannot exceed 100 characters")
    private String cvVersion;

    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;
}
