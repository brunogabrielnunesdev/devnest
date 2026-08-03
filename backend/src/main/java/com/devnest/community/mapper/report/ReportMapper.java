package com.devnest.community.mapper.report;

import com.devnest.community.dto.report.ReportResponse;
import com.devnest.community.entity.report.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReportMapper {

	@Mapping(target = "reporterId", source = "reporter.id")
	@Mapping(target = "postId", source = "post.id")
	@Mapping(target = "commentId", source = "comment.id")
	@Mapping(target = "reviewedById", source = "reviewedBy.id")
	ReportResponse toResponse(Report report);
}
