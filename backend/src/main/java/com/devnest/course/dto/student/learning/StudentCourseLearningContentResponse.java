package com.devnest.course.dto.student.learning;

import com.devnest.course.entity.course.CourseStatus;
import java.util.List;
import java.util.UUID;

public record StudentCourseLearningContentResponse(
	UUID courseId,
	String title,
	String description,
	String coverImageUrl,
	CourseStatus status,
	List<StudentCourseLearningModuleResponse> modules
) {
}
