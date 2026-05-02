package com.devnest.course.mapper;

import com.devnest.course.domain.QuizOption;
import com.devnest.course.dto.QuizOptionCreateRequest;
import com.devnest.course.dto.QuizOptionResponse;
import com.devnest.course.dto.QuizOptionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizOptionMapper {

	QuizOption toEntity(QuizOptionCreateRequest request);

	QuizOption toEntity(QuizOptionUpdateRequest request);

	@Mapping(target = "questionId", source = "question.id")
	QuizOptionResponse toResponse(QuizOption option);
}
