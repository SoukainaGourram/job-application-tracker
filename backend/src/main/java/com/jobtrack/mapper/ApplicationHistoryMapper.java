package com.jobtrack.mapper;

import com.jobtrack.dto.response.ApplicationHistoryResponse;
import com.jobtrack.entity.ApplicationHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationHistoryMapper {

    @Mapping(target = "applicationId", source = "application.id")
    ApplicationHistoryResponse toResponse(ApplicationHistory history);
}
