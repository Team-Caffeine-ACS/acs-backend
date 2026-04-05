package com.caffeine.acs_backend.dto.documenttype;

import com.caffeine.acs_backend.entity.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Identity document type for selection in person registration")
public record DocumentTypeResponse(
    @Schema(description = "Unique identifier") UUID id,
    @Schema(description = "Identity document type name", example = "Passport") String name) {

  public static DocumentTypeResponse from(DocumentType documentType) {
    return new DocumentTypeResponse(documentType.getId(), documentType.getName());
  }
}
