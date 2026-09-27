package com.jobtrack.dto.response;

import com.jobtrack.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistoryResponse {
    private Long id;
    private Long applicationId;
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private Instant changedAt;
    private String note;
}
