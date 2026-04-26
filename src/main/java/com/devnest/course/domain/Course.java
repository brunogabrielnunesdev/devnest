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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "courses")
@NoArgsConstructor
public class Course extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private User teacher;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(length = 40)
	private String level;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseStatus status;

	public static Course draft(User teacher, String title, String description, String level) {
		Course course = new Course();
		course.teacher = teacher;
		course.title = title;
		course.description = description;
		course.level = level;
		course.status = CourseStatus.DRAFT;
		return course;
	}

	public void update(String title, String description, String level) {
		this.title = title;
		this.description = description;
		this.level = level;
	}

	public void archive() {
		this.status = CourseStatus.ARCHIVED;
	}
}
