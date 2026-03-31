package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.service.DocumentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
@Tag(name = "Document Types")
@SecurityRequirement(name = "bearerAuth")
public class DocumentTypeController {

  private final DocumentTypeService documentTypeService;

  @Operation(
      summary = "List all document types",
      description = "Returns all document types sorted alphabetically")
  @ApiResponse(responseCode = "200", description = "Document type list returned")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @GetMapping
  public ResponseEntity<List<DocumentTypeResponse>> getAll() {
    return ResponseEntity.ok(documentTypeService.getAll());
  }
}
