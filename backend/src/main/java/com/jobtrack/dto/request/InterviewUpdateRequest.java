package com.jobtrack.dto.request;

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
public class InterviewUpdateRequest {

    private InterviewType type;

    private Instant scheduledAt;

    private Instant endedAt;

    private String location;

    private String interviewerName;

    private String notes;

    private String feedback;
}
