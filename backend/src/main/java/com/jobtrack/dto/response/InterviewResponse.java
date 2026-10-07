package com.jobtrack.dto.response;

import com.jobtrack.entity.InterviewStatus;
import com.jobtrack.entity.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private InterviewType type;
    private InterviewStatus status;
    private Instant scheduledAt;
    private Instant endedAt;
    private String location;
    private String interviewerName;
    private String notes;
    private String feedback;
    private Instant createdAt;
    private Instant updatedAt;
}
