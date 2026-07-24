package com.devnest.identity.repository;

import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findByIdAndStatus(UUID id, UserStatus status);

	@Query(
		value = """
			select u
			from User u
			left join fetch u.profile profile
			where (
				:query is null
				or lower(u.email) like lower(concat('%', :query, '%'))
				or lower(coalesce(profile.displayName, '')) like lower(concat('%', :query, '%'))
				or lower(coalesce(profile.fullName, '')) like lower(concat('%', :query, '%'))
			)
			order by u.createdAt desc
			""",
		countQuery = """
			select count(u)
			from User u
			left join u.profile profile
			where (
				:query is null
				or lower(u.email) like lower(concat('%', :query, '%'))
				or lower(coalesce(profile.displayName, '')) like lower(concat('%', :query, '%'))
				or lower(coalesce(profile.fullName, '')) like lower(concat('%', :query, '%'))
			)
			"""
	)
	Page<User> findAdminUsers(@Param("query") String query, Pageable pageable);

	@Query("""
		select u
		from User u
		left join fetch u.profile profile
		where (
			:query is null
			or lower(u.email) like lower(concat('%', :query, '%'))
			or lower(coalesce(profile.displayName, '')) like lower(concat('%', :query, '%'))
			or lower(coalesce(profile.fullName, '')) like lower(concat('%', :query, '%'))
		)
		order by u.createdAt desc
		""")
	List<User> findAllAdminUsers(@Param("query") String query);
}
