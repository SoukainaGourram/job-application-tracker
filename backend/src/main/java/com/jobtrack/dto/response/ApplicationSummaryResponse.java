package com.jobtrack.dto.response;

import com.jobtrack.entity.ApplicationStatus;
import com.jobtrack.entity.ContractType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryResponse {
    private Long id;
    private Long offerId;
    private String offerTitle;
    private String companyName;
    private String city;
    private String country;
    private ContractType contractType;
    private ApplicationStatus status;
    private LocalDate appliedAt;
    private List<String> technologies;
    private Instant createdAt;
}
