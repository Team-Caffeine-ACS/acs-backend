package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.nationality.NationalityResponse;
import com.caffeine.acs_backend.repository.NationalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NationalityService {

  private final NationalityRepository nationalityRepository;

  public List<NationalityResponse> getAllNationalities() {
    return nationalityRepository.findAllByOrderByNameAsc().stream()
        .map(NationalityResponse::from)
        .toList();
  }
}
