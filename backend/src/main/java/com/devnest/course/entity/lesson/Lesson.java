package com.devnest.course.entity.lesson;

import com.devnest.common.entity.BaseEntity;
import com.devnest.course.entity.module.Module;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "lessons")
@NoArgsConstructor
public class Lesson extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "module_id", nullable = false)
	private Module module;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(columnDefinition = "text")
	private String content;

	@Column(name = "video_url")
	private String videoUrl;

	@Column(nullable = false)
	private Integer position;
}

