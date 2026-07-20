package com.devnest.community.entity.tag;

import com.devnest.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "community_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityTag extends BaseEntity {

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, unique = true, length = 60)
	private String slug;

	public static CommunityTag create(String name, String slug) {
		CommunityTag tag = new CommunityTag();
		tag.name = name;
		tag.slug = slug;
		return tag;
	}

	public void rename(String name, String slug) {
		this.name = name;
		this.slug = slug;
	}
}
