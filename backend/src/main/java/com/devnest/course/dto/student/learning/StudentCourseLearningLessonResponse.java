package com.devnest.course.dto.student.learning;

import java.util.UUID;

public record StudentCourseLearningLessonResponse(
	UUID lessonId,
	String title,
	String description,
	String content,
	String videoUrl,
	Integer position,
	Boolean completed
) {
}
