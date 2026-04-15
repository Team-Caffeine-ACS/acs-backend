package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.dto.visit.DashboardRecentVisitResponse;
import com.caffeine.acs_backend.entity.Visit;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final VisitRepository visitRepository;

    public DashboardSummaryResponse getSummary(UUID buildingId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        
        // 1. Tänased andmed
        long todayVisits = visitRepository.countVisitsInPeriod(todayStart, now, buildingId);
        long active = visitRepository.countByStatusAndBuilding(VisitStatus.ACTIVE, buildingId);
        long pending = visitRepository.countByStatusAndBuilding(VisitStatus.PRE_REGISTERED, buildingId);

        // 2. Eelmise nädala andmed (viimased 7 päeva enne tänast)
        LocalDateTime lastWeekStart = todayStart.minusDays(7);
        long lastWeekTotal = visitRepository.countVisitsInPeriod(lastWeekStart, todayStart, buildingId);
        double lastWeekAverage = lastWeekTotal / 7.0;

        // 3. Arvutame trendi
        String visitorTrend = calculateTrend(todayVisits, lastWeekAverage);

        return new DashboardSummaryResponse(
            active,
            todayVisits, // Bookings today
            pending,
            Math.max(0, 200 - (int) active), // Capacity 200
            Map.of("visitors", visitorTrend)
        );
    }


  public List<DashboardRecentVisitResponse> getRecentVisits(UUID buildingId, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

    return visitRepository.findRecentVisits(startOfDay, buildingId, pageable).stream()
        .map(
            visit -> {
              // Võtame nime PersonData-st
              var person = visit.getVisitor().getPerson();
              String fullName = person.getGivenName() + " " + person.getSurname();

              // Võtame organisatsiooni nime
              String orgName =
                  person.getOrganization() != null ? person.getOrganization().getName() : "Private";

              return new DashboardRecentVisitResponse(
                  fullName,
                  orgName,
                  visit.getArrivalTime(),
                  visit.getExitTime(),
                  mapStatus(visit),
                  visit.getVisitor().getId());
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
