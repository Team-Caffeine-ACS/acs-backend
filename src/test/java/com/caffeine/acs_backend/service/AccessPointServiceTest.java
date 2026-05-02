package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.dto.accesspoint.AccessPointResponse;
import com.caffeine.acs_backend.entity.AccessPoint;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessPointServiceTest {

  @Mock private AccessPointRepository accessPointRepository;

  @InjectMocks private AccessPointService accessPointService;

  @Test
  void getAllAccessPoints_returnsMappedResponses() {
    // GIVEN
    AccessPoint ap1 = new AccessPoint();
    ap1.setId(UUID.randomUUID());
    ap1.setName("A-Sissepääs");

    AccessPoint ap2 = new AccessPoint();
    ap2.setId(UUID.randomUUID());
    ap2.setName("B-Sissepääs");

    // Mockime repositooriumi käitumise
    when(accessPointRepository.findAllByOrderByNameAsc()).thenReturn(List.of(ap1, ap2));

    // ACT
    List<AccessPointResponse> results = accessPointService.getAllAccessPoints();

    // ASSERT
    assertThat(results).hasSize(2);
    assertThat(results.get(0).name()).isEqualTo("A-Sissepääs");
    assertThat(results.get(1).name()).isEqualTo("B-Sissepääs");
  }
}
