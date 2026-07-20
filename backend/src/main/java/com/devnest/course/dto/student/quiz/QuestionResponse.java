package com.devnest.course.dto.student.quiz;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
	UUID id,
	String statement,
	Integer position,
	List<QuizOptionResponse> options
) {
}

