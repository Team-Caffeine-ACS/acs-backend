package com.caffeine.acs_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ScheduledVisitorServiceTest {

  @Mock private PreRegistrationService preRegistrationService;

  @Mock private VisitRepository visitRepository;

  @InjectMocks private ScheduledVisitorService service;

  @Test
  void shouldMapGetAll() {
    when(preRegistrationService.getAll(any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of()));

    var result = service.getAll(null, null, null, null, Pageable.unpaged());

    assertNotNull(result);
    verify(preRegistrationService).getAll(any(), any(), any(), any(), any());
  }

  @Test
  void shouldReturnStats() {
    when(visitRepository.countByStatus(VisitStatus.ACTIVE)).thenReturn(5L);
    when(visitRepository.countByStatus(VisitStatus.CANCELLED)).thenReturn(2L);
    when(visitRepository.countByArrivalTimeBetween(any(), any())).thenReturn(10L);

    var stats = service.getStats();

    assertEquals(10L, stats.get("todayVisitors"));
    assertEquals(5L, stats.get("activeVisits"));
  }

  @Test
  void export_shouldReturnBytes() {
    when(preRegistrationService.getAll(any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of()));

    byte[] result = service.export(null, null, null, null);

    assertNotNull(result);
  }
}
