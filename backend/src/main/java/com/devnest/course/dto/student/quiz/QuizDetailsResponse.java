package com.devnest.course.dto.student.quiz;

import java.util.List;
import java.util.UUID;

public record QuizDetailsResponse(
	UUID id,
	UUID lessonId,
	String title,
	Integer passingScore,
	Integer maxAttempts,
	Integer maxQuestions,
	List<QuestionResponse> questions
) {
}

