package com.devnest.community.mapper.comment;

import com.devnest.community.dto.comment.CommentResponse;
import com.devnest.community.entity.comment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CommentMapper {

	@Mapping(target = "postId", source = "post.id")
	@Mapping(target = "authorId", source = "author.id")
	CommentResponse toResponse(Comment comment);
}
