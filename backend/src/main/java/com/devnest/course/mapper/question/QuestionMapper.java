package com.devnest.course.mapper.question;

import com.devnest.course.dto.question.QuestionCreateRequest;
import com.devnest.course.entity.quiz.QuizQuestion;
import com.devnest.course.dto.question.QuestionResponse;
import com.devnest.course.dto.question.QuestionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper {

	QuizQuestion toEntity(QuestionCreateRequest request);

	QuizQuestion toEntity(QuestionUpdateRequest request);

	@Mapping(target = "quizId", source = "quiz.id")
	QuestionResponse toResponse(QuizQuestion question);
}

