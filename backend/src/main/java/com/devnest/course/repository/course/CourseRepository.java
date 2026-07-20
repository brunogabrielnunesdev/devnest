package com.devnest.course.repository.course;

import com.devnest.course.entity.course.Course;
import com.devnest.course.entity.course.CourseStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, UUID> {

	java.util.List<Course> findAllByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

	long countByTeacherId(UUID teacherId);

	java.util.List<Course> findAllByStatusAndArchivedFalseOrderByCreatedAtDesc(CourseStatus status);

	Optional<Course> findByIdAndTeacherId(UUID id, UUID teacherId);

	Optional<Course> findByIdAndStatusAndArchivedFalse(UUID id, CourseStatus status);

	@Query("""
		select c
		from Course c
		where (:query is null or lower(c.title) like lower(concat('%', :query, '%')))
		order by c.createdAt desc
		""")
	Page<Course> findAdminCourses(@Param("query") String query, Pageable pageable);

	@Query("""
		select c
		from Course c
		join fetch c.teacher teacher
		left join fetch teacher.profile profile
		where (:query is null or lower(c.title) like lower(concat('%', :query, '%')))
		order by c.createdAt desc
		""")
	List<Course> findAllAdminCourses(@Param("query") String query);

	List<Course> findAllByStatusOrderByCreatedAtDesc(CourseStatus status);
}
