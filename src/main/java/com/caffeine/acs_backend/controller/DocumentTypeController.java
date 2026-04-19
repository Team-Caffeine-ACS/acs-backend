package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.service.DocumentTypeService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
@Tag(name = "Identity document Types")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class DocumentTypeController {

  private final DocumentTypeService documentTypeService;

  @Operation(
      summary = "List all identity document types",
      description = "Returns all identity document types sorted alphabetically")
  @ApiResponse(responseCode = "200", description = "Identity document type list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @GetMapping
  public ResponseEntity<List<DocumentTypeResponse>> getAll() {
    return ResponseEntity.ok(documentTypeService.getAll());
  }

  @Operation(summary = "Create a document type")
  @ApiResponse(responseCode = "201", description = "Document type created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping
  public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody LookupRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.create(request));
  }

  @Operation(summary = "Update a document type")
  @ApiResponse(responseCode = "200", description = "Document type updated")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Document type not found")
  @PutMapping("/{id}")
  public ResponseEntity<DocumentTypeResponse> update(
      @PathVariable UUID id, @Valid @RequestBody LookupRequest request) {
    return ResponseEntity.ok(documentTypeService.update(id, request));
  }

  @Operation(summary = "Delete a document type")
  @ApiResponse(responseCode = "204", description = "Document type deleted")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Document type not found")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    documentTypeService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
