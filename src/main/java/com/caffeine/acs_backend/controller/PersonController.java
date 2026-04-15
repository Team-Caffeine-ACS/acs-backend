package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.person.CreatePersonRequest;
import com.caffeine.acs_backend.dto.person.PersonInRoleResponse;
import com.caffeine.acs_backend.dto.person.PersonResponse;
import com.caffeine.acs_backend.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      summary = "Create a new person",
      description = "Creates a new person record, optionally with a document.")
  @ApiResponse(responseCode = "201", description = "Person created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PostMapping
  public ResponseEntity<PersonResponse> createPerson(
      @Valid @RequestBody CreatePersonRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(request));
  }

  @Operation(
      summary = "Search employees by name",
      description = "Returns persons with the Employee role whose first or last name contains the query string.")
  @ApiResponse(responseCode = "200", description = "Search results returned (may be empty)")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/employees/search")
  public ResponseEntity<List<PersonInRoleResponse>> searchEmployees(
      @Parameter(description = "Name fragment to search for", example = "Alice") @RequestParam
          String q) {
    return ResponseEntity.ok(personService.search(q, "Employee"));
  }

  @Operation(
      summary = "Search visitors by name",
      description = "Returns persons with the Visitor role whose first or last name contains the query string.")
  @ApiResponse(responseCode = "200", description = "Search results returned (may be empty)")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/visitors/search")
  public ResponseEntity<List<PersonInRoleResponse>> searchVisitors(
      @Parameter(description = "Name fragment to search for", example = "Dave") @RequestParam
          String q) {
    return ResponseEntity.ok(personService.search(q, "Visitor"));
  }
}
