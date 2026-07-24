package com.devnest.community.repository.userrelation;

import com.devnest.community.entity.userrelation.UserMute;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMuteRepository extends JpaRepository<UserMute, UUID> {

	Optional<UserMute> findByUserIdAndMutedUserId(UUID userId, UUID mutedUserId);

	Page<UserMute> findAllByUserId(UUID userId, Pageable pageable);
}
