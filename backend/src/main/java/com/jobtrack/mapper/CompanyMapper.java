package com.jobtrack.mapper;

import com.jobtrack.dto.request.CompanyCreateRequest;
import com.jobtrack.dto.request.CompanyUpdateRequest;
import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.dto.response.CompanySummaryResponse;
import com.jobtrack.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CompanyContactMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CompanyUpdateRequest request, @MappingTarget Company company);

    @Mapping(target = "offersCount", ignore = true)
    CompanyResponse toResponse(Company company);

    @Mapping(target = "offersCount", ignore = true)
    @Mapping(target = "contactsCount", ignore = true)
    CompanySummaryResponse toSummaryResponse(Company company);

    Company toEntity(CompanyResponse response);
}
