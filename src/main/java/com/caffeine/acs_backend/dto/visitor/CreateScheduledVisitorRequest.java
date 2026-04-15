package com.caffeine.acs_backend.dto.visitor;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateScheduledVisitorRequest {

  @NotNull private UUID personId;

  @NotNull private UUID buildingId;

  private UUID hostId;

  @NotNull private LocalDateTime scheduledTime;

  private String notes;

  public CreatePreRegistrationRequest toPreRegistrationRequest() {
    return new CreatePreRegistrationRequest(personId, scheduledTime, hostId, notes, buildingId);
  }
}
