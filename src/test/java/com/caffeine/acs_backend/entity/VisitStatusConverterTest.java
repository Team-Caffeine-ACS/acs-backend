package com.caffeine.acs_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.VisitStatus;
import org.junit.jupiter.api.Test;

class VisitStatusConverterTest {

  private final VisitStatusConverter converter = new VisitStatusConverter();

  @Test
  void convertToEntityAttribute_mapsLegacyPreRegisteredToPlanned() {
    assertThat(converter.convertToEntityAttribute("PRE_REGISTERED")).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void convertToDatabaseColumn_persistsCanonicalPlannedValue() {
    assertThat(converter.convertToDatabaseColumn(VisitStatus.PLANNED)).isEqualTo("PLANNED");
  }
}
