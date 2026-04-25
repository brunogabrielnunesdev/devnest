package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
import com.devnest.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
	name = "course_enrollments",
	uniqueConstraints = @UniqueConstraint(name = "uk_course_enrollments_student_course", columnNames = {"student_id", "course_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEnrollment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private User student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EnrollmentStatus status;

	@Column(name = "enrolled_at", nullable = false)
	private OffsetDateTime enrolledAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;
}
