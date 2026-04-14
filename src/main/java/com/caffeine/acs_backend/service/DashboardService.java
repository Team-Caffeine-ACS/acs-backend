package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.dashboard.DashboardSummaryResponse;
import com.caffeine.acs_backend.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VisitRepository visitRepository;

    public DashboardSummaryResponse getSummary(UUID buildingId) {
        // Määrame tänase päeva vahemiku
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // Küsime andmed repositoryst
        long activeVisitors = visitRepository.countCurrentActiveVisitors(buildingId);
        long bookingsToday = visitRepository.countBookingsByDate(startOfDay, endOfDay, buildingId);
        
        // Esialgu paneme staatilised väärtused asjadele, mida meil veel andmebaasis pole
        long pending = 0; 
        long available = 100;

        return new DashboardSummaryResponse(
            activeVisitors,
            bookingsToday,
            pending,
            available,
            Map.of("visitors", "+5%", "bookings", "-2%") // Näidistrendid
        );
    }
}