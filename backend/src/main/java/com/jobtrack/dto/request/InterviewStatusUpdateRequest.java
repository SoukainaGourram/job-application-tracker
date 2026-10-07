package com.jobtrack.dto.request;

import com.jobtrack.entity.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private InterviewStatus status;

    private String notes;
}
