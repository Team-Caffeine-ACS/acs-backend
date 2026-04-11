package com.caffeine.acs_backend.dto.person;

import com.caffeine.acs_backend.entity.PersonInRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Person in a role — used for host/assignor search results")
public record PersonInRoleResponse(
    @Schema(description = "PersonInRole unique identifier") UUID id,
    @Schema(description = "Person unique identifier") UUID personId,
    @Schema(description = "First name", example = "Jane") String givenName,
    @Schema(description = "Last name", example = "Doe") String surname,
    @Schema(description = "Role name", example = "Receptionist") String roleName,
    @Schema(description = "Email address", example = "john@gmail.com") String email) {

  public static PersonInRoleResponse from(PersonInRole pir) {
    return new PersonInRoleResponse(
        pir.getId(),
        pir.getPerson().getId(),
        pir.getPerson().getGivenName(),
        pir.getPerson().getSurname(),
        pir.getRole().getName(),
        pir.getPerson().getEmail());
  }
}
