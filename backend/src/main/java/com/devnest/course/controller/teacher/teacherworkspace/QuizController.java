package com.devnest.course.controller.teacher.teacherworkspace;

import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.dto.quiz.QuizResponse;
import com.devnest.course.dto.quiz.QuizUpdateRequest;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.course.service.quiz.QuizService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
@RequestMapping("/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz")
public class QuizController {

	private final QuizMapper quizMapper;
	private final QuizService quizService;

	@PostMapping
	public ResponseEntity<QuizResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody QuizCreateRequest request
	) {
		var quiz = quizMapper.toEntity(request);
		var response = quizService.create(courseId, moduleId, lessonId, quiz);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<QuizResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		var response = quizService.findById(courseId, moduleId, lessonId);
		return ResponseEntity.ok(response);
	}

	@PutMapping
	public ResponseEntity<QuizResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody QuizUpdateRequest request
	) {
		var quiz = quizMapper.toEntity(request);
		var response = quizService.update(courseId, moduleId, lessonId, quiz);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		quizService.delete(courseId, moduleId, lessonId);
		return ResponseEntity.noContent().build();
	}
}

