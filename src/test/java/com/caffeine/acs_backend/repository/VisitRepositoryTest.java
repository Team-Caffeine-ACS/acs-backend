package com.caffeine.acs_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

class VisitRepositoryTest {

  @Test
  void findPreRegistrations_UsesBuildingIdBindingForSecondParameter() throws NoSuchMethodException {
    Method method =
        VisitRepository.class.getMethod(
            "findPreRegistrations",
            String.class,
            UUID.class,
            java.time.LocalDateTime.class,
            java.time.LocalDateTime.class,
            String.class,
            Pageable.class);

    Annotation[] secondParameterAnnotations = method.getParameterAnnotations()[1];
    Param paramAnnotation = (Param) secondParameterAnnotations[0];

    assertThat(secondParameterAnnotations).hasSize(1);
    assertThat(paramAnnotation.value()).isEqualTo("buildingId");
    assertThat(method.getReturnType()).isEqualTo(Page.class);
  }

  @Test
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

    assertThat(findAllFiltered.getAnnotation(Query.class).value()).doesNotContain("PRE_REGISTERED");
    assertThat(findAllFiltered.getAnnotation(Query.class).countQuery())
        .doesNotContain("PRE_REGISTERED");
    assertThat(findPreRegistrations.getAnnotation(Query.class).value())
        .doesNotContain("PRE_REGISTERED");
    assertThat(findPreRegistrations.getAnnotation(Query.class).countQuery())
        .doesNotContain("PRE_REGISTERED");
  }
}
