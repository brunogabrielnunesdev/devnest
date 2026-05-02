package com.devnest.course.mapper;

import com.devnest.course.domain.Quiz;
import com.devnest.course.dto.QuizCreateRequest;
import com.devnest.course.dto.QuizResponse;
import com.devnest.course.dto.QuizUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizMapper {

	Quiz toEntity(QuizCreateRequest request);

	Quiz toEntity(QuizUpdateRequest request);

	@Mapping(target = "lessonId", source = "lesson.id")
	QuizResponse toResponse(Quiz quiz);
}
