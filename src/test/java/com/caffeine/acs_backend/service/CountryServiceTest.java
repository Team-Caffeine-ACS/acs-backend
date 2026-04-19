package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.Country;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.CountryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

  @Mock private CountryRepository countryRepository;
  @InjectMocks private CountryService countryService;

  private Country country(String name) {
    Country c = Country.builder().name(name).build();
    c.setId(UUID.randomUUID());
    return c;
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsMappedList() {
    when(countryRepository.findAllByOrderByNameAsc())
        .thenReturn(List.of(country("Estonia"), country("Latvia")));

    List<CountryResponse> result = countryService.getAllCountries();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(CountryResponse::name).containsExactly("Estonia", "Latvia");
  }

  @Test
  void getAll_empty_returnsEmptyList() {
    when(countryRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    assertThat(countryService.getAllCountries()).isEmpty();
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_savesAndReturnsResponse() {
    Country saved = country("Estonia");
    when(countryRepository.save(any())).thenReturn(saved);

    CountryResponse result = countryService.create(new LookupRequest("Estonia"));

    assertThat(result.name()).isEqualTo("Estonia");
    assertThat(result.id()).isEqualTo(saved.getId());
    verify(countryRepository).save(any());
  }

  // ── update ────────────────────────────────────────────────────────────────────

  @Test
  void update_changesName() {
    Country existing = country("Old Name");
    when(countryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(countryRepository.save(existing)).thenReturn(existing);

    CountryResponse result = countryService.update(existing.getId(), new LookupRequest("New Name"));

    assertThat(result.name()).isEqualTo("New Name");
    verify(countryRepository).save(existing);
  }

  @Test
  void update_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(countryRepository.findById(id)).thenReturn(Optional.empty());

    var request = new LookupRequest("X");
    assertThatThrownBy(() -> countryService.update(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── delete ────────────────────────────────────────────────────────────────────

  @Test
  void delete_callsDeleteById() {
    Country existing = country("Estonia");
    when(countryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    countryService.delete(existing.getId());

    verify(countryRepository).deleteById(existing.getId());
  }

  @Test
  void delete_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(countryRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> countryService.delete(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(countryRepository, never()).deleteById(any());
  }
}
