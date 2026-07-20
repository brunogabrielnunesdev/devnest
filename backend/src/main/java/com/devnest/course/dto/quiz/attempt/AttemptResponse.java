package com.devnest.course.dto.quiz.attempt;

import com.devnest.course.dto.quiz.QuizAnswerResultResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AttemptResponse(
	UUID id,
	UUID quizId,
	UUID studentId,
	Integer attemptNumber,
	Integer score,
	Boolean passed,
	Integer remainingAttempts,
	Boolean reviewAvailable,
	List<QuizAnswerResultResponse> answers,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

