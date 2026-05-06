package com.caffeine.acs_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.VisitStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VisitTest {

  @Test
  void builder_defaultsStatusToPlanned() {
    Visit visit = Visit.builder().build();

    assertThat(visit.getStatus()).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void equals_usesBaseEntityId() {
    UUID id = UUID.randomUUID();
    Visit first = Visit.builder().build();
    Visit second = Visit.builder().build();
    first.setId(id);
    second.setId(id);

    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
  }
}
