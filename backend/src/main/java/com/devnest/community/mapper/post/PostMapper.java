package com.devnest.community.mapper.post;

import com.devnest.community.dto.post.PostResponse;
import com.devnest.community.entity.post.Post;
import com.devnest.community.mapper.forum.ForumMapper;
import com.devnest.community.mapper.tag.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
		componentModel = "spring",
		uses = {ForumMapper.class, TagMapper.class},
		unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PostMapper {

	@Mapping(target = "authorId", source = "author.id")
	@Mapping(target = "projectId", source = "project.id")
	@Mapping(target = "courseId", source = "course.id")
    PostResponse toResponse(Post post);
}
