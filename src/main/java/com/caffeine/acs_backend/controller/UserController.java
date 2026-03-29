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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
