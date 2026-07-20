package com.devnest.course.entity.course;

import com.devnest.common.entity.BaseEntity;
import com.devnest.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

	@Column(name = "cover_image_url", length = 500)
	private String coverImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseStatus status;

	@Column(nullable = false)
	private boolean archived;

	public static Course draft(User teacher, String title, String description, String level, String coverImageUrl) {
		Course course = new Course();
		course.teacher = teacher;
		course.title = title;
		course.description = description;
		course.level = level;
		course.coverImageUrl = coverImageUrl;
		course.status = CourseStatus.DRAFT;
		course.archived = false;
		return course;
	}

	public void update(String title, String description, String level, String coverImageUrl) {
		this.title = title;
		this.description = description;
		this.level = level;
		this.coverImageUrl = coverImageUrl;
	}

	public void archive() {
		this.archived = true;
	}

	public void restore() {
		this.archived = false;
		if (this.status == CourseStatus.ARCHIVED) {
			this.status = CourseStatus.DRAFT;
		}
	}
}

