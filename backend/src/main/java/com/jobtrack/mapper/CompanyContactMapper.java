package com.jobtrack.mapper;

import com.jobtrack.dto.request.CompanyContactCreateRequest;
import com.jobtrack.dto.request.CompanyContactUpdateRequest;
import com.jobtrack.dto.response.CompanyContactResponse;
import com.jobtrack.entity.CompanyContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyContactMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CompanyContact toEntity(CompanyContactCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CompanyContactUpdateRequest request, @MappingTarget CompanyContact contact);

    @Mapping(target = "companyId", source = "company.id")
    CompanyContactResponse toResponse(CompanyContact contact);
}
