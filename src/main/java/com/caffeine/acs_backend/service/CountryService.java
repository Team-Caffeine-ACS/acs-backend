package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.Country;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.CountryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CountryService {

  private final CountryRepository countryRepository;

  public List<CountryResponse> getAllCountries() {
    return countryRepository.findAllByOrderByNameAsc().stream().map(CountryResponse::from).toList();
  }

  @Transactional
  public CountryResponse create(LookupRequest request) {
    Country country = Country.builder().name(request.name()).build();
    return CountryResponse.from(countryRepository.save(country));
  }

  @Transactional
  public CountryResponse update(UUID id, LookupRequest request) {
    Country country = findById(id);
    country.setName(request.name());
    return CountryResponse.from(countryRepository.save(country));
  }

  @Transactional
  public void delete(UUID id) {
    findById(id);
    countryRepository.deleteById(id);
  }

  private Country findById(UUID id) {
    return countryRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Country not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
