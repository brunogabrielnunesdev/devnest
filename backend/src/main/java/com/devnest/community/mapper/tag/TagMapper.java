package com.devnest.community.mapper.tag;

import com.devnest.community.dto.tag.TagResponse;
import com.devnest.community.entity.tag.CommunityTag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TagMapper {

	TagResponse toResponse(CommunityTag tag);
}
