package com.caffeine.acs_backend.dto.person;

import com.caffeine.acs_backend.entity.Person;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Created person details")
public record PersonResponse(
    @Schema(description = "Person unique identifier") UUID id,
    @Schema(description = "First name", example = "John") String givenName,
    @Schema(description = "Last name", example = "Smith") String surname,
    @Schema(description = "Email address", example = "john@example.com") String email) {

  public static PersonResponse from(Person person) {
    return new PersonResponse(
        person.getId(), person.getGivenName(), person.getSurname(), person.getEmail());
  }
}
