package com.devnest.community.mapper.forum;

import com.devnest.community.dto.forum.ForumResponse;
import com.devnest.community.entity.forum.Forum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ForumMapper {

	@Mapping(target = "createdById", source = "createdBy.id")
    ForumResponse toResponse(Forum forum);
}
