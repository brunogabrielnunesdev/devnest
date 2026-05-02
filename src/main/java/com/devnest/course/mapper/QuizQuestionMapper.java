package com.devnest.course.mapper;

import com.devnest.course.domain.QuizQuestion;
import com.devnest.course.dto.QuizQuestionCreateRequest;
import com.devnest.course.dto.QuizQuestionResponse;
import com.devnest.course.dto.QuizQuestionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizQuestionMapper {

	QuizQuestion toEntity(QuizQuestionCreateRequest request);

	QuizQuestion toEntity(QuizQuestionUpdateRequest request);

	@Mapping(target = "quizId", source = "quiz.id")
	QuizQuestionResponse toResponse(QuizQuestion question);
}
