package com.jobtrack.dto.request;

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
public class ApplicationUpdateRequest {

    private LocalDate appliedAt;

    private String notes;

    private String coverLetter;

    @Size(max = 100, message = "CV version cannot exceed 100 characters")
    private String cvVersion;

    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;
}
