package com.devnest.course.controller.teacher.teacherworkspace;

import com.devnest.course.dto.question.QuestionCreateRequest;
import com.devnest.course.dto.question.QuestionResponse;
import com.devnest.course.dto.question.QuestionUpdateRequest;
import com.devnest.course.mapper.question.QuestionMapper;
import com.devnest.course.service.question.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question")
public class QuestionController {

	private final QuestionMapper questionMapper;
	private final QuestionService questionService;

	@PostMapping
	public ResponseEntity<QuestionResponse> create(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@Valid @RequestBody QuestionCreateRequest request
	) {
		var question = questionMapper.toEntity(request);
		var response = questionService.create(courseId, moduleId, lessonId, question);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<QuestionResponse>> findAll(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId
	) {
		var response = questionService.findAll(courseId, moduleId, lessonId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{questionId}")
	public ResponseEntity<QuestionResponse> findById(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		var response = questionService.findById(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{questionId}")
	public ResponseEntity<QuestionResponse> update(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId,
		@Valid @RequestBody QuestionUpdateRequest request
	) {
		var question = questionMapper.toEntity(request);
		var response = questionService.update(courseId, moduleId, lessonId, questionId, question);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{questionId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID courseId,
		@PathVariable UUID moduleId,
		@PathVariable UUID lessonId,
		@PathVariable UUID questionId
	) {
		questionService.delete(courseId, moduleId, lessonId, questionId);
		return ResponseEntity.noContent().build();
	}
}
