package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.organization.OrganizationResponse;
import com.caffeine.acs_backend.entity.Organization;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.OrganizationRepository;
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
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;
  @InjectMocks private OrganizationService organizationService;

  private Organization organization(String name) {
    Organization o = Organization.builder().name(name).build();
    o.setId(UUID.randomUUID());
    return o;
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsMappedList() {
    when(organizationRepository.findAllByOrderByNameAsc())
        .thenReturn(List.of(organization("Acme Corp"), organization("Beta LLC")));

    List<OrganizationResponse> result = organizationService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(OrganizationResponse::name)
        .containsExactly("Acme Corp", "Beta LLC");
  }

  @Test
  void getAll_empty_returnsEmptyList() {
    when(organizationRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    assertThat(organizationService.getAll()).isEmpty();
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_savesAndReturnsResponse() {
    Organization saved = organization("Acme Corp");
    when(organizationRepository.save(any())).thenReturn(saved);

    OrganizationResponse result = organizationService.create(new LookupRequest("Acme Corp"));

    assertThat(result.name()).isEqualTo("Acme Corp");
    assertThat(result.id()).isEqualTo(saved.getId());
    verify(organizationRepository).save(any());
  }

  // ── update ────────────────────────────────────────────────────────────────────

  @Test
  void update_changesName() {
    Organization existing = organization("Old Name");
    when(organizationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(organizationRepository.save(existing)).thenReturn(existing);

    OrganizationResponse result =
        organizationService.update(existing.getId(), new LookupRequest("New Name"));

    assertThat(result.name()).isEqualTo("New Name");
    verify(organizationRepository).save(existing);
  }

  @Test
  void update_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(organizationRepository.findById(id)).thenReturn(Optional.empty());

    var request = new LookupRequest("X");
    assertThatThrownBy(() -> organizationService.update(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── delete ────────────────────────────────────────────────────────────────────

  @Test
  void delete_callsDeleteById() {
    Organization existing = organization("Acme Corp");
    when(organizationRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    organizationService.delete(existing.getId());

    verify(organizationRepository).deleteById(existing.getId());
  }

  @Test
  void delete_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(organizationRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.delete(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(organizationRepository, never()).deleteById(any());
  }
}
