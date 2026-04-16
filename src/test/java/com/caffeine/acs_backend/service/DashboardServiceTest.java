package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock private AccessPointRepository accessPointRepository;

  @InjectMocks private DashboardService dashboardService;

  @Test
  void getSummary_accessPointNotFound_throwsNotFound() {

    UUID fakeId = UUID.randomUUID();
    when(accessPointRepository.existsById(fakeId)).thenReturn(false);

    assertThatThrownBy(() -> dashboardService.getSummary(fakeId))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> {
              BusinessException be = (BusinessException) ex;
              assertThat(be.getMessage()).isEqualTo("Access point not found");
              assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
              assertThat(be.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            });
  }
}
