package com.caffeine.acs_backend.dto.role;

import com.caffeine.acs_backend.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Role record")
public record RoleResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Role name", example = "Visitor") String name) {

  public static RoleResponse from(Role r) {
    return new RoleResponse(r.getId(), r.getName());
  }
}
