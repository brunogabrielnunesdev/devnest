package com.devnest.admin.controller.user;

import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.dto.user.UserResponse;
import com.devnest.admin.dto.user.roleupdate.UserRoleUpdateRequest;
import com.devnest.admin.service.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminUserController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/users")
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<AdminPageResponse<UserResponse>> findAll(
		@RequestParam(required = false) String query,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(userService.findAll(query, page, size));
	}

	@GetMapping("/all")
	public ResponseEntity<List<UserResponse>> findAllList(
		@RequestParam(required = false) String query
	) {
		return ResponseEntity.ok(userService.findAllList(query));
	}

	@PatchMapping("/{id}/role")
	public ResponseEntity<UserResponse> updateRole(
		@PathVariable UUID id,
		@Valid @RequestBody UserRoleUpdateRequest request
	) {
		return ResponseEntity.ok(userService.updateRole(id, request.role()));
	}
}
