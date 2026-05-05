package com.caffeine.acs_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
  void convertToEntityAttribute_returnsNullForBlankDatabaseValue() {
    assertThat(converter.convertToEntityAttribute("   ")).isNull();
  }

  @Test
  void convertToEntityAttribute_normalizesTrimmedCaseInsensitiveValues() {
    assertThat(converter.convertToEntityAttribute(" planned ")).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void convertToEntityAttribute_throwsDescriptiveExceptionForUnknownValue() {
    assertThatThrownBy(() -> converter.convertToEntityAttribute("UNKNOWN_STATE_123"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unknown VisitStatus found in database: UNKNOWN_STATE_123");
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
