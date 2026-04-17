package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.user.AdminCreateUserRequest;
import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth") // JWT tugi Swaggeris
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "Get current authenticated user",
      description = "Returns profile information of the currently logged-in user.")
  @ApiResponse(responseCode = "200", description = "User profile returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(userService.getMe(currentUser));
  }

  @Operation(
      summary = "Update current authenticated user",
      description = "Allows the logged-in user to update their profile information.")
  @ApiResponse(responseCode = "200", description = "User profile updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid update data")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @PatchMapping("/me")
  public ResponseEntity<UserResponse> updateMe(
      @AuthenticationPrincipal User currentUser, @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.updateMe(currentUser, request));
  }

  @Operation(
      summary = "Admin: list all users",
      description = "Returns a list of all registered users.")
  @ApiResponse(responseCode = "200", description = "User list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin")
  public ResponseEntity<List<UserResponse>> adminGetAllUsers() {
    return ResponseEntity.ok(userService.adminGetAllUsers());
  }

  @Operation(
      summary = "Admin: create a new user",
      description =
          "Allows an admin to create a new user with a specified role. Defaults to VISITOR if role is omitted.")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
  @ApiResponse(responseCode = "409", description = "Email already in use")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin")
  public ResponseEntity<UserResponse> adminCreateUser(
      @Valid @RequestBody AdminCreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.adminCreateUser(request));
  }

  @Operation(
      summary = "Admin: update any user's email or password",
      description = "Allows an admin to update the email and/or password of any user by their ID.")
  @ApiResponse(responseCode = "200", description = "User updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid update data")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/admin/{id}")
  public ResponseEntity<UserResponse> adminUpdateUser(
      @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.adminUpdateUser(id, request));
  }

  @Operation(
      summary = "Admin: delete a user",
      description = "Allows an admin to delete a user by their ID.")
  @ApiResponse(responseCode = "204", description = "User deleted successfully")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/admin/{id}")
  public ResponseEntity<Void> adminDeleteUser(@PathVariable UUID id) {
    userService.adminDeleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
