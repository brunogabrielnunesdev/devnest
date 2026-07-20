package com.devnest.admin.controller.course;

import com.devnest.admin.dto.course.CourseResponse;
import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.service.course.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminCourseController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/courses")
public class CourseController {

	private final CourseService courseService;

	@GetMapping
	public ResponseEntity<AdminPageResponse<CourseResponse>> findAll(
		@RequestParam(required = false) String query,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(courseService.findAll(query, page, size));
	}

	@GetMapping("/all")
	public ResponseEntity<List<CourseResponse>> findAllList(
		@RequestParam(required = false) String query
	) {
		return ResponseEntity.ok(courseService.findAllList(query));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CourseResponse> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(courseService.findById(id));
	}

	@PatchMapping("/{id}/archive")
	public ResponseEntity<CourseResponse> archive(@PathVariable UUID id) {
		return ResponseEntity.ok(courseService.archive(id));
	}

	@PatchMapping("/{id}/restore")
	public ResponseEntity<CourseResponse> restore(@PathVariable UUID id) {
		return ResponseEntity.ok(courseService.restore(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		courseService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
