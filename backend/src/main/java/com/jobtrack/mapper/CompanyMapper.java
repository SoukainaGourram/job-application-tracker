package com.jobtrack.mapper;

import com.jobtrack.dto.response.CompanyResponse;
import com.jobtrack.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);

    Company toEntity(CompanyResponse response);
}
