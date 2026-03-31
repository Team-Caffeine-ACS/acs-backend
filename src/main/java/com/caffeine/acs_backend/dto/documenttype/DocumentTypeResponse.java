package com.caffeine.acs_backend.dto.documenttype;

import com.caffeine.acs_backend.entity.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Document type for selection in person registration")
public record DocumentTypeResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Document type name", example = "Passport") String name) {

  public static DocumentTypeResponse from(DocumentType documentType) {
    return new DocumentTypeResponse(documentType.getId(), documentType.getName());
  }
}
