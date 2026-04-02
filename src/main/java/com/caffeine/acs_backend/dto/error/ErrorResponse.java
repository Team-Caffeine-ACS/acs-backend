package com.caffeine.acs_backend.dto.error;

import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;

@Schema(description = "Standard API error response")
@Builder
public class ErrorResponse {

  @Schema(description = "Timestamp of the error", example = "2024-03-31T12:00:00Z")
  private Instant timestamp;

  @Schema(description = "Human-readable error message", example = "Invalid credentials")
  private String message;

  @Schema(description = "HTTP status code", example = "401")
  private int status;

  @Schema(description = "Machine-readable error code", example = "RESOURCE_NOT_FOUND")
  private ErrorCode errorCode;

  @Schema(description = "Request path", example = "/api/users/123")
  private String path;

  @Schema(description = "Additional error details (e.g. validation errors)")
  private Map<String, String> details;

  // getters (Lombok @Builder ei loo neid automaatselt)
  public Instant getTimestamp() {
    return timestamp;
  }

  public String getMessage() {
    return message;
  }

  public int getStatus() {
    return status;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public String getPath() {
    return path;
  }

  public Map<String, String> getDetails() {
    return details;
  }
}
