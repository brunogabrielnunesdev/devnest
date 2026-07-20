package com.devnest.course.dto.student.quiz;

import java.util.UUID;

public record QuizOptionResponse(
	UUID id,
	String text,
	Integer position
) {
}

