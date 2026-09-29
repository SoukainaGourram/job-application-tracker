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
public class CompanyContactResponse {

    private Long id;
    private Long companyId;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String email;
    private String phone;
    private String linkedinUrl;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
