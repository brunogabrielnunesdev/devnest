package com.devnest.course.mapper.option;

import com.devnest.course.entity.quiz.option.Option;
import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.dto.option.OptionResponse;
import com.devnest.course.dto.option.OptionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OptionMapper {

	Option toEntity(OptionCreateRequest request);

	Option toEntity(OptionUpdateRequest request);

	@Mapping(target = "questionId", source = "question.id")
	OptionResponse toResponse(Option option);
}

