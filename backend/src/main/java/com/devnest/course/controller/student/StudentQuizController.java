package com.devnest.course.controller.student;

import com.devnest.course.dto.quiz.attempt.AttemptResponse;
import com.devnest.course.dto.quiz.attempt.AttemptSubmitRequest;
import com.devnest.course.dto.student.quiz.QuizDetailsResponse;
import com.devnest.course.service.student.StudentQuizService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@RequestMapping("/course/{courseId}/lesson/{lessonId}/quiz")
public class StudentQuizController {

	private final StudentQuizService studentQuizService;

	@GetMapping
	public ResponseEntity<QuizDetailsResponse> getQuiz(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId
	) {
		var response = studentQuizService.getQuiz(courseId, lessonId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/attempts")
	public ResponseEntity<AttemptResponse> submitAttempt(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody AttemptSubmitRequest request
	) {
		var response = studentQuizService.submitAttempt(courseId, lessonId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/attempts")
	public ResponseEntity<List<AttemptResponse>> findMyAttempts(
		@PathVariable UUID courseId,
		@PathVariable UUID lessonId
	) {
		var response = studentQuizService.findMyAttempts(courseId, lessonId);
		return ResponseEntity.ok(response);
	}
}

