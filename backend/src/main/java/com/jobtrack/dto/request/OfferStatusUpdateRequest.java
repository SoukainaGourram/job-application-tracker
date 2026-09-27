package com.jobtrack.dto.request;

import com.jobtrack.entity.OfferStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OfferStatus status;
}
