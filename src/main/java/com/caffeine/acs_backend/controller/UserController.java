package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
}
