package com.devnest.course.dto.quiz.attempt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AttemptSubmitRequest(
	@NotEmpty
	List<@Valid AttemptAnswerRequest> answers
) {
}

