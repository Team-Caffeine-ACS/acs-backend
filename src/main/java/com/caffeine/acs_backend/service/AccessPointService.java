package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.accesspoint.AccessPointResponse;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessPointService {

  private final AccessPointRepository accessPointRepository;

  public List<AccessPointResponse> getAllAccessPoints() {
    return accessPointRepository.findAllByOrderByNameAsc().stream()
        .map(AccessPointResponse::from)
        .toList();
  }
}
