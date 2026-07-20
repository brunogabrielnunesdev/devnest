package com.devnest.course.mapper.module;

import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.entity.module.Module;
import com.devnest.course.dto.module.ModuleResponse;
import com.devnest.course.dto.module.ModuleUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModuleMapper {

	Module toEntity(ModuleCreateRequest request);

	Module toEntity(ModuleUpdateRequest request);

	@Mapping(target = "courseId", source = "course.id")
	ModuleResponse toResponse(Module module);
}

