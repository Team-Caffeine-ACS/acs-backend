package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final VisitRepository visitRepository;
  private final AccessPointRepository accessPointRepository;

  public DashboardSummaryResponse getSummary(UUID accessPointId) {
    if (accessPointId != null && !accessPointRepository.existsById(accessPointId)) {
      throw new BusinessException(
          "Access point not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime lastWeekStart = todayStart.minusDays(7);

    long active = 0;
    long todayVisits = 0;
    long pending = 0;
    long lastWeekTotal = 0;

    PageRequest countOnly = PageRequest.of(0, 1);
    // 1. IN_BUILDING (Aktiivsed)
    active =
        visitRepository
            .findAllFiltered(
                null, VisitStatus.IN_BUILDING.name(), null, null, accessPointId, countOnly)
            .getTotalElements();

    // 2. PLANNED (Ootel)
    pending =
        visitRepository
            .findAllFiltered(null, VisitStatus.PLANNED.name(), null, null, accessPointId, countOnly)
            .getTotalElements();

    // 3. Tänased visiidid kokku
    todayVisits =
        visitRepository
            .findAllFiltered(null, null, todayStart, null, accessPointId, countOnly)
            .getTotalElements();

    // 4. Viimase nädala visiidid (perioodi põhjal)
    lastWeekTotal =
        visitRepository
            .findAllFiltered(null, null, lastWeekStart, todayStart, accessPointId, countOnly)
            .getTotalElements();

    double lastWeekAverage = lastWeekTotal / 7.0;
    String visitorTrend = calculateTrend(todayVisits, lastWeekAverage);

    return new DashboardSummaryResponse(
        active, todayVisits, pending, 0, Map.of("visitors", visitorTrend));
  }

  private String calculateTrend(long current, double baseline) {
    if (baseline == 0) return "0%"; // Väldime nulliga jagamist
    double change = ((current - baseline) / baseline) * 100;

    // Formaatime ilusa stringi, nt "+15.2%" või "-5.0%"
    return String.format("%s%.1f%%", change >= 0 ? "+" : "", change);
  }
}
