package com.jobtrack.dto.request;

import com.jobtrack.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewCreateRequest {

    @NotNull(message = "Interview type is required")
    private InterviewType type;

    @NotNull(message = "Scheduled date/time is required")
    private Instant scheduledAt;

    private Instant endedAt;

    private String location;

    private String interviewerName;

    private String notes;
}
