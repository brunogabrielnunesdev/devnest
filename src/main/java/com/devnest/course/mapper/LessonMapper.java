package com.devnest.course.mapper;

import com.devnest.course.domain.Lesson;
import com.devnest.course.dto.LessonCreateRequest;
import com.devnest.course.dto.LessonResponse;
import com.devnest.course.dto.LessonUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LessonMapper {

	Lesson toEntity(LessonCreateRequest request);

	Lesson toEntity(LessonUpdateRequest request);

	@Mapping(target = "moduleId", source = "module.id")
	LessonResponse toResponse(Lesson lesson);
}
