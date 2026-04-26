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

    long active;
    long todayVisits;
    long pending;
    long lastWeekTotal;

    if (accessPointId != null) {
      // Kui ID on antud, filtreerime selle punkti järgi
      active =
          visitRepository.countByStatusAndAccessPointId(VisitStatus.IN_BUILDING, accessPointId);
      todayVisits = visitRepository.countTodayVisitsByAccessPointId(todayStart, accessPointId);
      pending = visitRepository.countByStatusAndAccessPointId(VisitStatus.PLANNED, accessPointId);

      lastWeekTotal =
          visitRepository.countVisitsInPeriodByAccessPointId(
              lastWeekStart, todayStart, accessPointId);
    } else {
      // Kui ID-d pole, näitame globaalset statistikat (nagu varem tegime)
      active = visitRepository.countByStatus(VisitStatus.IN_BUILDING);
      todayVisits = visitRepository.countTodayBookings(todayStart);
      pending = visitRepository.countByStatus(VisitStatus.PLANNED);
      lastWeekTotal = visitRepository.countVisitsInPeriod(lastWeekStart, todayStart);
    }

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
