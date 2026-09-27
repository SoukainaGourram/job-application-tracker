package com.jobtrack.mapper;

import com.jobtrack.dto.request.OfferCreateRequest;
import com.jobtrack.dto.request.OfferUpdateRequest;
import com.jobtrack.dto.response.OfferResponse;
import com.jobtrack.dto.response.OfferSummaryResponse;
import com.jobtrack.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OfferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Offer toEntity(OfferCreateRequest request);

    OfferResponse toResponse(Offer offer);

    OfferSummaryResponse toSummaryResponse(Offer offer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(OfferUpdateRequest request, @MappingTarget Offer offer);
}
