package com.jobtrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySummaryResponse {

    private Long id;
    private String name;
    private String website;
    private String industry;
    private String location;
    private String size;
    private long offersCount;
    private long contactsCount;
    private Instant createdAt;
}
