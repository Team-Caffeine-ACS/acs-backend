package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.repository.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryService {

  private final CountryRepository countryRepository;

  public List<CountryResponse> getAllCountries() {
    return countryRepository.findAllByOrderByNameAsc().stream().map(CountryResponse::from).toList();
  }
}
