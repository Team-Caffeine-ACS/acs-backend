package com.caffeine.acs_backend.dto.enums;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.VisitStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VisitStatusTest {

  @ParameterizedTest
  @CsvSource({"PLANNED, Ootel", "IN_BUILDING, Hoones", "DEPARTED, Lahkunud", "EXPIRED, Aegunud"})
  void getLabel_returnsCorrectEstonianLabel(VisitStatus status, String expectedLabel) {
    assertThat(status.getLabel()).isEqualTo(expectedLabel);
  }

  @Test
  void enumValues_containsAllStatusTypes() {
    // See test tagab, et me pole ühtegi väärtust kogemata lisamata jätnud
    assertThat(VisitStatus.values()).hasSize(4);
    assertThat(VisitStatus.valueOf("PLANNED")).isEqualTo(VisitStatus.PLANNED);
  }
}
