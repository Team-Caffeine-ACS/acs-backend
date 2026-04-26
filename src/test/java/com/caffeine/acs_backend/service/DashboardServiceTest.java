package com.caffeine.acs_backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.VisitListView;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock private VisitRepository visitRepository;
  @Mock private AccessPointRepository accessPointRepository;
  @InjectMocks private DashboardService dashboardService;

  @Test
  void getSummary_WhenAccessPointNotFound_ThrowsException() {
    UUID id = UUID.randomUUID();
    when(accessPointRepository.existsById(id)).thenReturn(false);

    assertThrows(BusinessException.class, () -> dashboardService.getSummary(id));
  }

  @Test
  void getSummary_FullCoverage_Test() {
    UUID id = UUID.randomUUID();
    when(accessPointRepository.existsById(id)).thenReturn(true);

    // 1. POSITIIVNE TREND (+50.0%)
    setupMock(15, 70);
    DashboardSummaryResponse res1 = dashboardService.getSummary(id);

    assertEquals(15, res1.bookingsToday()); // Täna visiite
    assertEquals("+50.0%", res1.trendIndicators().get("visitors"));

    // 2. NULLIGA JAGAMINE (0%)
    setupMock(10, 0);
    DashboardSummaryResponse res2 = dashboardService.getSummary(id);

    assertEquals("0%", res2.trendIndicators().get("visitors"));

    // 3. NEGATIIVNE TREND (-50.0%)
    setupMock(5, 70);
    DashboardSummaryResponse res3 = dashboardService.getSummary(id);

    assertEquals("-50.0%", res3.trendIndicators().get("visitors"));
  }

  private void setupMock(long today, long lastWeek) {
    // Kuna su DashboardService kutsub findAllFiltered 4 korda,
    // peame moki seadistama nii, et see annaks vastused järjekorras:
    // 1. active, 2. pending, 3. today, 4. lastWeek
    Page<VisitListView> mockPageToday = new PageImpl<>(List.of(), PageRequest.of(0, 1), today);
    Page<VisitListView> mockPageWeek = new PageImpl<>(List.of(), PageRequest.of(0, 1), lastWeek);

    when(visitRepository.findAllFiltered(any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of())) // activeVisitors -> 0
        .thenReturn(new PageImpl<>(List.of())) // pendingRequests -> 0
        .thenReturn(mockPageToday) // bookingsToday -> 'today' parameeter
        .thenReturn(mockPageWeek); // trendi arvutuseks -> 'lastWeek' parameeter
  }
}
