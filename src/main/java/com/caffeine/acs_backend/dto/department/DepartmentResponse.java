package com.caffeine.acs_backend.dto.department;

import com.caffeine.acs_backend.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Department record")
public record DepartmentResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Department name", example = "Engineering") String name) {

  public static DepartmentResponse from(Department d) {
    return new DepartmentResponse(d.getId(), d.getName());
  }
}
