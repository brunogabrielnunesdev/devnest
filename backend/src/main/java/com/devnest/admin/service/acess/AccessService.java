package com.devnest.admin.service.acess;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("adminAccessService")
@RequiredArgsConstructor
public class AccessService {

	private final CustomUserProvider customUserProvider;

	public User getAuthenticatedAdmin() {
		User user = customUserProvider.getAuthenticatedUser();
		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Only admins can access administrative resources.");
		}
		return user;
	}
}
