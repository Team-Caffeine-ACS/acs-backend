package com.caffeine.acs_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.caffeine.acs_backend.enums.VisitStatus;
import org.junit.jupiter.api.Test;

class VisitStatusConverterTest {

  private final VisitStatusConverter converter = new VisitStatusConverter();

  @Test
  void convertToEntityAttribute_mapsLegacyPreRegisteredToPlanned() {
    assertThat(converter.convertToEntityAttribute("PRE_REGISTERED")).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void convertToEntityAttribute_returnsNullForNullDatabaseValue() {
    assertThat(converter.convertToEntityAttribute(null)).isNull();
  }

  @Test
  void convertToEntityAttribute_normalizesTrimmedCaseInsensitiveValues() {
    assertThat(converter.convertToEntityAttribute(" planned ")).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void convertToEntityAttribute_throwsExceptionForUnknownValue() {
    // Muudame IllegalArgumentException -> IllegalStateException
    assertThrows(
        IllegalStateException.class,
        () -> {
          converter.convertToEntityAttribute("TOTALLY_UNKNOWN");
        });
  }

  @Test
  void convertToDatabaseColumn_persistsCanonicalPlannedValue() {
    assertThat(converter.convertToDatabaseColumn(VisitStatus.PLANNED)).isEqualTo("PLANNED");
  }

  @Test
  void convertToDatabaseColumn_returnsNullForNullAttribute() {
    assertThat(converter.convertToDatabaseColumn(null)).isNull();
  }
}
