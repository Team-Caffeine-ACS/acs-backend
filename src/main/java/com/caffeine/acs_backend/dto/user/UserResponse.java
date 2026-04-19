package com.caffeine.acs_backend.dto.user;

import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Response object representing a user profile")
public record UserResponse(
    @Schema(
            description = "Unique identifier of the user",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "User email address", example = "john@example.com") String email,
    @Schema(description = "Role assigned to the user", example = "ADMIN") UserRole role,
    @Schema(
            description = "Identifier of the related person entity",
            example = "550e8400-e29b-41d4-a716-446655440111")
        UUID personId,
    @Schema(description = "Person details linked to this user, null if no person is linked")
        PersonDetails person) {

  @Schema(description = "Personal details from the person record")
  public record PersonDetails(
      @Schema(description = "Given name", example = "John") String givenName,
      @Schema(description = "Surname", example = "Doe") String surname,
      @Schema(description = "Job title", example = "Software Engineer") String jobTitle,
      @Schema(description = "Social security number", example = "38001085718")
          String socialSecurityNumber,
      @Schema(description = "Department name", example = "Engineering") String department,
      @Schema(description = "Organization name", example = "Acme Corp") String organization) {

    public static PersonDetails from(Person p) {
      return new PersonDetails(
          p.getGivenName(),
          p.getSurname(),
          p.getJobTitle(),
          p.getSocialSecurityNumber(),
          p.getDepartment() != null ? p.getDepartment().getName() : null,
          p.getOrganization() != null ? p.getOrganization().getName() : null);
    }
  }

  public static UserResponse from(User user) {
    Person p = user.getPerson();
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getRole(),
        p != null ? p.getId() : null,
        p != null ? PersonDetails.from(p) : null);
  }
}
