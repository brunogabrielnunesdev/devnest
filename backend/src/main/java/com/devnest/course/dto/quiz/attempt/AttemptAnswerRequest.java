package com.devnest.course.dto.quiz.attempt;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttemptAnswerRequest(
	@NotNull
	UUID questionId,

	@NotNull
	UUID selectedOptionId
) {
}

