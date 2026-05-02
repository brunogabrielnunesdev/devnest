package com.devnest.course.controller;

import com.devnest.course.dto.QuizOptionCreateRequest;
import com.devnest.course.dto.QuizOptionResponse;
import com.devnest.course.dto.QuizOptionUpdateRequest;
import com.devnest.course.mapper.QuizOptionMapper;
import com.devnest.course.service.QuizOptionService;
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
@RequestMapping("/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/quiz/questions/{questionId}/options")
public class QuizOptionController {

	private final QuizOptionMapper quizOptionMapper;
	private final QuizOptionService quizOptionService;

	@PostMapping
	public ResponseEntity<QuizOptionResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@Valid @RequestBody QuizOptionCreateRequest request
	) {
		var option = quizOptionMapper.toEntity(request);
		var response = quizOptionService.create(courseId, moduleId, lessonId, questionId, option);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<QuizOptionResponse>> findAll(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		var response = quizOptionService.findAll(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{optionId}")
	public ResponseEntity<QuizOptionResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId
	) {
		var response = quizOptionService.findById(courseId, moduleId, lessonId, questionId, optionId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{optionId}")
	public ResponseEntity<QuizOptionResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId,
		@Valid @RequestBody QuizOptionUpdateRequest request
	) {
		var option = quizOptionMapper.toEntity(request);
		var response = quizOptionService.update(courseId, moduleId, lessonId, questionId, optionId, option);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{optionId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@PathVariable UUID optionId
	) {
		quizOptionService.delete(courseId, moduleId, lessonId, questionId, optionId);
		return ResponseEntity.noContent().build();
	}
}
