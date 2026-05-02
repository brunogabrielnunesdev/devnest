package com.devnest.course.controller;

import com.devnest.course.dto.QuizQuestionCreateRequest;
import com.devnest.course.dto.QuizQuestionResponse;
import com.devnest.course.dto.QuizQuestionUpdateRequest;
import com.devnest.course.mapper.QuizQuestionMapper;
import com.devnest.course.service.QuizQuestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/quiz/questions")
public class QuizQuestionController {

	private final QuizQuestionMapper quizQuestionMapper;
	private final QuizQuestionService quizQuestionService;

	@PostMapping
	public ResponseEntity<QuizQuestionResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody QuizQuestionCreateRequest request
	) {
		var question = quizQuestionMapper.toEntity(request);
		var response = quizQuestionService.create(courseId, moduleId, lessonId, question);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<QuizQuestionResponse>> findAll(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		var response = quizQuestionService.findAll(courseId, moduleId, lessonId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{questionId}")
	public ResponseEntity<QuizQuestionResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		var response = quizQuestionService.findById(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{questionId}")
	public ResponseEntity<QuizQuestionResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@Valid @RequestBody QuizQuestionUpdateRequest request
	) {
		var question = quizQuestionMapper.toEntity(request);
		var response = quizQuestionService.update(courseId, moduleId, lessonId, questionId, question);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{questionId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		quizQuestionService.delete(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.noContent().build();
	}
}
