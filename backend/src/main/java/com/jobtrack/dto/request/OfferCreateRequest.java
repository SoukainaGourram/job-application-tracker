package com.jobtrack.dto.request;

import com.jobtrack.entity.ContractType;
import com.jobtrack.entity.OfferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    @Size(min = 1, max = 150, message = "Company name must be between 1 and 150 characters")
    private String companyName;

    private Long companyId;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    @NotNull(message = "Technologies list must not be null")
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    private String description;

    @Pattern(
        regexp = "^(https?://.+)?$",
        message = "Job URL must be a valid HTTP or HTTPS URL"
    )
    @Size(max = 500, message = "Job URL cannot exceed 500 characters")
    private String jobUrl;

    @Size(max = 100, message = "Salary cannot exceed 100 characters")
    private String salary;

    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;

    private LocalDate applicationDeadline;

    @Builder.Default
    private OfferStatus status = OfferStatus.SAVED;
}
