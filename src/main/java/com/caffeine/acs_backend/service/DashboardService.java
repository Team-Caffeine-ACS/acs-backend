package com.caffeine.acs_backend.service;
import com.caffeine.acs_backend.repository.*;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VisitRepository visitRepository;
    //private final BookingRepository bookingRepository;
    //private final ZoneRepository zoneRepository;
}
