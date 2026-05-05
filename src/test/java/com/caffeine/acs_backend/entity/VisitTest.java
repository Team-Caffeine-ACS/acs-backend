package com.caffeine.acs_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.VisitStatus;
import org.junit.jupiter.api.Test;

class VisitTest {

  @Test
  void builder_defaultsStatusToPlanned() {
    Visit visit = Visit.builder().build();

    assertThat(visit.getStatus()).isEqualTo(VisitStatus.PLANNED);
  }
}
