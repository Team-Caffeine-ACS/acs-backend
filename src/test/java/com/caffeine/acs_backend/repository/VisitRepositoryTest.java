package com.caffeine.acs_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

class VisitRepositoryTest {

  @Test
  @DisplayName("Kontrolli findPreRegistrations parameetrite sidumist ja tüüpi")
  void findPreRegistrations_HasCorrectBuildingIdParamBinding() throws NoSuchMethodException {
    // Kasutame peegeldust, et leida meetod
    Method method =
        VisitRepository.class.getMethod(
            "findPreRegistrations",
            String.class,
            UUID.class,
            java.time.LocalDateTime.class,
            java.time.LocalDateTime.class,
            String.class,
            Pageable.class);

    // Kontrollime, et parameetrite hulgast löytyks @Param("buildingId")
    // See on robustsem kui indeksi [1][0] kasutamine
    boolean hasBuildingIdAnnotation =
        Arrays.stream(method.getParameters())
            .anyMatch(
                p ->
                    p.isAnnotationPresent(Param.class)
                        && p.getAnnotation(Param.class).value().equals("buildingId"));

    assertThat(hasBuildingIdAnnotation)
        .as("Meetodil findPreRegistrations peab olema @Param('buildingId') annotatsioon")
        .isTrue();

    assertThat(method.getReturnType()).isEqualTo(Page.class);
  }

  @Test
  @DisplayName("Veendu, et natiivsed päringud ei kasuta enam vana PRE_REGISTERED staatust")
  void repositoryQueries_doNotReferenceLegacyPreRegisteredStatus() throws NoSuchMethodException {
    Method findAllFiltered =
        VisitRepository.class.getMethod(
            "findAllFiltered",
            String.class,
            String.class,
            java.time.LocalDateTime.class,
            java.time.LocalDateTime.class,
            UUID.class,
            Pageable.class);

    Method findPreRegistrations =
        VisitRepository.class.getMethod(
            "findPreRegistrations",
            String.class,
            UUID.class,
            java.time.LocalDateTime.class,
            java.time.LocalDateTime.class,
            String.class,
            Pageable.class);

    // Kontrollime peamist SQL-i ja countQuery-t mõlemas meetodis
    assertQueryDoesNotContainLegacyStatus(findAllFiltered.getAnnotation(Query.class));
    assertQueryDoesNotContainLegacyStatus(findPreRegistrations.getAnnotation(Query.class));
  }

  private void assertQueryDoesNotContainLegacyStatus(Query query) {
    String legacyStatus = "PRE_REGISTERED";
    assertThat(query.value())
        .as("Põhipäring ei tohi sisaldada staatust " + legacyStatus)
        .doesNotContain(legacyStatus);

    assertThat(query.countQuery())
        .as("Count-päring ei tohi sisaldada staatust " + legacyStatus)
        .doesNotContain(legacyStatus);
  }
}
