package com.devnest.course.dto.quiz;

import java.util.UUID;

public record QuizAnswerResultResponse(
	UUID questionId,
	UUID selectedOptionId,
	Boolean correct
) {
}

