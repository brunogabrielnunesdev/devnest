package com.devnest.admin.service.user;

import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.dto.user.UserResponse;
import com.devnest.admin.service.acess.AccessService;
import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUserService")
@RequiredArgsConstructor
public class UserService {

	private final AccessService accessService;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public AdminPageResponse<UserResponse> findAll(String query, int page, int size) {
		accessService.getAuthenticatedAdmin();

		var userPage = userRepository.findAdminUsers(normalizeQuery(query), PageRequest.of(page, size));
		return new AdminPageResponse<>(
			userPage.getContent().stream().map(this::toResponse).toList(),
			userPage.getNumber(),
			userPage.getSize(),
			userPage.getTotalElements(),
			userPage.getTotalPages()
		);
	}

	@Transactional(readOnly = true)
	public java.util.List<UserResponse> findAllList(String query) {
		accessService.getAuthenticatedAdmin();
		String normalizedQuery = normalizeQuery(query);

		return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
			.stream()
			.filter(user -> matchesQuery(user, normalizedQuery))
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public UserResponse updateRole(UUID userId, UserRole role) {
		User admin = accessService.getAuthenticatedAdmin();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found."));

		if (admin.getId().equals(user.getId()) && role != UserRole.ADMIN) {
			throw new ConflictException("Admins cannot remove their own admin role.");
		}

		user.setRole(role);
		return toResponse(user);
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}
		return query.trim().toLowerCase();
	}

	private boolean matchesQuery(User user, String query) {
		if (query == null) {
			return true;
		}

		var profile = user.getProfile();
		String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
		String displayName = profile != null && profile.getDisplayName() != null ? profile.getDisplayName().toLowerCase() : "";
		String fullName = profile != null && profile.getFullName() != null ? profile.getFullName().toLowerCase() : "";

		return email.contains(query) || displayName.contains(query) || fullName.contains(query);
	}

	private UserResponse toResponse(User user) {
		var profile = user.getProfile();
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			profile != null ? profile.getDisplayName() : null,
			profile != null ? profile.getFullName() : null,
			user.getRole(),
			user.getStatus(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
