package com.devnest.course.mapper.quiz;

import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.dto.quiz.QuizResponse;
import com.devnest.course.dto.quiz.QuizUpdateRequest;
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

