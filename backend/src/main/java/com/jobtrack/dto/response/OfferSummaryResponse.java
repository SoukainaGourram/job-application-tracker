package com.jobtrack.dto.response;

import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.OfferStatus;
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
public class OfferSummaryResponse {
    private Long id;
    private String title;
    private String companyName;
    private String city;
    private String country;
    private ContractType contractType;
    private List<String> technologies;
    private OfferStatus status;
    private String source;
    private LocalDate applicationDeadline;
    private Instant createdAt;
}
