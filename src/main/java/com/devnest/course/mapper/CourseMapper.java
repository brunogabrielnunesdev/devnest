package com.devnest.course.mapper;

import com.devnest.course.domain.Course;
import com.devnest.course.dto.CourseCreateRequest;
import com.devnest.course.dto.CourseResponse;
import com.devnest.course.dto.CourseUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

	Course toEntity(CourseCreateRequest request);

	Course toEntity(CourseUpdateRequest request);

	void updateCourse(CourseUpdateRequest request, @MappingTarget Course course);

	@Mapping(target = "teacherId", source = "teacher.id")
	CourseResponse toResponse(Course course);
}
