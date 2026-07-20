package com.devnest.course.mapper.course;

import com.devnest.course.entity.course.Course;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.course.CourseResponse;
import com.devnest.course.dto.course.update.CourseUpdateRequest;
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

