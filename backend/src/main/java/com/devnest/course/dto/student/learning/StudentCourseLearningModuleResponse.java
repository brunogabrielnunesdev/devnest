package com.devnest.course.dto.student.learning;

import java.util.List;
import java.util.UUID;

public record StudentCourseLearningModuleResponse(
	UUID moduleId,
	String title,
	String description,
	Integer position,
	List<StudentCourseLearningLessonResponse> lessons
) {
}
