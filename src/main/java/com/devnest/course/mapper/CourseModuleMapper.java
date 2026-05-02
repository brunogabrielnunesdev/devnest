package com.devnest.course.mapper;

import com.devnest.course.domain.CourseModule;
import com.devnest.course.dto.CourseModuleCreateRequest;
import com.devnest.course.dto.CourseModuleResponse;
import com.devnest.course.dto.CourseModuleUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseModuleMapper {

	CourseModule toEntity(CourseModuleCreateRequest request);

	CourseModule toEntity(CourseModuleUpdateRequest request);

	@Mapping(target = "courseId", source = "course.id")
	CourseModuleResponse toResponse(CourseModule module);
}
