package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "course_modules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseModule extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false)
	private Integer position;
}
