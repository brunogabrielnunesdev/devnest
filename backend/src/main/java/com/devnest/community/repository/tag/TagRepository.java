package com.devnest.community.repository.tag;

import com.devnest.community.entity.tag.CommunityTag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<CommunityTag, UUID> {

	Optional<CommunityTag> findBySlug(String slug);

	List<CommunityTag> findAllBySlugIn(Collection<String> slugs);

	boolean existsBySlug(String slug);
}
