package com.devnest.project.mapper;

import com.devnest.project.entity.project.ProjectUpdate;
import com.devnest.project.dto.project.updateproject.ProjectUpdateCreateRequest;
import com.devnest.project.dto.project.updateproject.ProjectUpdateResponse;
import com.devnest.project.dto.project.updateproject.ProjectUpdateUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectUpdateMapper {

	ProjectUpdate toEntity(ProjectUpdateCreateRequest request);

	ProjectUpdate toEntity(ProjectUpdateUpdateRequest request);

	@Mapping(target = "projectId", source = "project.id")
	ProjectUpdateResponse toResponse(ProjectUpdate projectUpdate);
}

