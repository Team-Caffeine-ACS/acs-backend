package com.caffeine.acs_backend.dto.visitor;

import com.caffeine.acs_backend.enums.VisitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
  @NotNull private VisitStatus status;
}
