package com.devnest.course.mapper.lesson;

import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.lesson.LessonResponse;
import com.devnest.course.dto.lesson.update.LessonUpdateRequest;
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

