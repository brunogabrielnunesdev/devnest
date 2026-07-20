package com.devnest.course.repository.course;

import com.devnest.course.entity.course.CourseEnrollment;
import com.devnest.course.entity.course.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {

	boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

	Optional<CourseEnrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

	List<CourseEnrollment> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

	long countByStudentIdAndStatusIn(UUID studentId, List<EnrollmentStatus> statuses);

	long countByCourseTeacherIdAndStatusIn(UUID teacherId, List<EnrollmentStatus> statuses);

	void deleteAllByCourseId(UUID courseId);
}

