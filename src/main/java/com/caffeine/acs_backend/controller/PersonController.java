package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.person.PersonInRoleResponse;
import com.caffeine.acs_backend.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Tag(name = "Persons", description = "Person search")
@SecurityRequirement(name = "bearerAuth")
public class PersonController {

  private final PersonService personService;

  @Operation(
      summary = "Search persons by name",
      description =
          "Returns person records whose first or last name contains the query string."
              + " Optionally filter by role name.")
  @ApiResponse(responseCode = "200", description = "Search results returned (may be empty)")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/search")
  public ResponseEntity<List<PersonInRoleResponse>> search(
      @Parameter(description = "Name fragment to search for", example = "Jane")
          @RequestParam String q,
      @Parameter(description = "Role name to filter by (optional)", example = "Employee")
          @RequestParam(required = false) String role) {
    return ResponseEntity.ok(personService.search(q, role));
  }
}
