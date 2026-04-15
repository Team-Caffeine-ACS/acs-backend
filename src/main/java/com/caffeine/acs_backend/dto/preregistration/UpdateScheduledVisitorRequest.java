package com.caffeine.acs_backend.dto.preregistration;

import com.caffeine.acs_backend.enums.VisitStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateScheduledVisitorRequest {

  private LocalDateTime expectedArrival;
  private UUID hostId;
  private UUID buildingId;
  private VisitStatus status;
}
