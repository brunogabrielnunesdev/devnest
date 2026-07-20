package com.devnest.admin.dto.user.roleupdate;

import com.devnest.identity.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
	@NotNull UserRole role
) {
}
