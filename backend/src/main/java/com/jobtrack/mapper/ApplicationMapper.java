package com.jobtrack.mapper;

import com.jobtrack.dto.request.ApplicationCreateRequest;
import com.jobtrack.dto.request.ApplicationUpdateRequest;
import com.jobtrack.dto.response.ApplicationResponse;
import com.jobtrack.dto.response.ApplicationSummaryResponse;
import com.jobtrack.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {OfferMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {

    ApplicationResponse toResponse(Application application);

    @Mapping(target = "offerId", source = "offer.id")
    @Mapping(target = "offerTitle", source = "offer.title")
    @Mapping(target = "companyName", source = "offer.companyName")
    @Mapping(target = "city", source = "offer.city")
    @Mapping(target = "country", source = "offer.country")
    @Mapping(target = "contractType", source = "offer.contractType")
    @Mapping(target = "technologies", source = "offer.technologies")
    ApplicationSummaryResponse toSummaryResponse(Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "offer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Application toEntity(ApplicationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "offer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ApplicationUpdateRequest request, @MappingTarget Application application);
}
