package com.devnest.community.mapper.reaction;

import com.devnest.community.dto.reaction.ReactionResponse;
import com.devnest.community.entity.reaction.Reaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReactionMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "postId", source = "post.id")
	@Mapping(target = "commentId", source = "comment.id")
	ReactionResponse toResponse(Reaction reaction);
}
