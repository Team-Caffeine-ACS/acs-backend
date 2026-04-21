package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.dto.visit.DashboardRecentVisitResponse;
import com.caffeine.acs_backend.entity.Visit;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
      active = visitRepository.countByStatusAndAccessPointId(VisitStatus.ACTIVE, accessPointId);
      todayVisits = visitRepository.countTodayVisitsByAccessPointId(todayStart, accessPointId);
      pending =
          visitRepository.countByStatusAndAccessPointId(VisitStatus.PRE_REGISTERED, accessPointId);

      lastWeekTotal =
          visitRepository.countVisitsInPeriodByAccessPointId(
              lastWeekStart, todayStart, accessPointId);
    } else {
      // Kui ID-d pole, näitame globaalset statistikat (nagu varem tegime)
      active = visitRepository.countByStatus(VisitStatus.ACTIVE);
      todayVisits = visitRepository.countTodayBookings(todayStart);
      pending = visitRepository.countByStatus(VisitStatus.PRE_REGISTERED);
      lastWeekTotal = visitRepository.countVisitsInPeriod(lastWeekStart, todayStart);
    }

    double lastWeekAverage = lastWeekTotal / 7.0;
    String visitorTrend = calculateTrend(todayVisits, lastWeekAverage);

    return new DashboardSummaryResponse(
        active, todayVisits, pending, 0, Map.of("visitors", visitorTrend));
  }

  public List<DashboardRecentVisitResponse> getRecentVisits(UUID accessPointId, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

    return visitRepository.findRecentVisits(startOfDay, accessPointId, pageable).stream()
        .map(
            visit -> {
              var person = visit.getVisitor().getPerson();
              String fullName = person.getGivenName() + " " + person.getSurname();

              String orgName =
                  person.getOrganization() != null ? person.getOrganization().getName() : "Private";

              String apName =
                  visit.getAccessPoint() != null ? visit.getAccessPoint().getName() : null;
              String apAddress =
                  visit.getAccessPoint() != null ? visit.getAccessPoint().getAddress() : null;

              return new DashboardRecentVisitResponse(
                  fullName,
                  orgName,
                  visit.getArrivalTime(),
                  visit.getExitTime(),
                  mapStatus(visit),
                  visit.getVisitor().getId(),
                  apName,
                  apAddress);
            })
        .toList();
  }

  private VisitStatus mapStatus(Visit visit) {
    if (visit.getExitTime() != null) return VisitStatus.COMPLETED;
    if (visit.getArrivalTime().isBefore(LocalDateTime.now())) return VisitStatus.ACTIVE;
    return VisitStatus.PRE_REGISTERED;
  }

  private String calculateTrend(long current, double baseline) {
    if (baseline == 0) return "0%"; // Väldime nulliga jagamist
    double change = ((current - baseline) / baseline) * 100;

    // Formaatime ilusa stringi, nt "+15.2%" või "-5.0%"
    return String.format("%s%.1f%%", change >= 0 ? "+" : "", change);
  }
}
