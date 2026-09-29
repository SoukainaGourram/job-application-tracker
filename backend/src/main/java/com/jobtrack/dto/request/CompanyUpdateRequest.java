package com.jobtrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 1, max = 150, message = "Company name must be between 1 and 150 characters")
    private String name;

    @Pattern(
        regexp = "^(https?://.+)?$",
        message = "Website must be a valid HTTP or HTTPS URL"
    )
    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;

    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 50, message = "Size cannot exceed 50 characters")
    private String size;

    private String description;

    private String notes;
}
