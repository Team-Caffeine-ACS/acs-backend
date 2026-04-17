package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.role.RoleResponse;
import com.caffeine.acs_backend.entity.Role;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.RoleRepository;
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
class RoleServiceTest {

  @Mock private RoleRepository roleRepository;
  @InjectMocks private RoleService roleService;

  private Role role(String name) {
    Role r = Role.builder().name(name).build();
    r.setId(UUID.randomUUID());
    return r;
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsMappedList() {
    when(roleRepository.findAll()).thenReturn(List.of(role("Guard"), role("Manager")));

    List<RoleResponse> result = roleService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(RoleResponse::name).containsExactlyInAnyOrder("Guard", "Manager");
  }

  @Test
  void getAll_returnsSortedAlphabetically() {
    when(roleRepository.findAll()).thenReturn(List.of(role("Zebra"), role("Alpha")));

    List<RoleResponse> result = roleService.getAll();

    assertThat(result).extracting(RoleResponse::name).containsExactly("Alpha", "Zebra");
  }

  @Test
  void getAll_empty_returnsEmptyList() {
    when(roleRepository.findAll()).thenReturn(List.of());

    assertThat(roleService.getAll()).isEmpty();
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_savesAndReturnsResponse() {
    Role saved = role("Security Guard");
    when(roleRepository.save(any())).thenReturn(saved);

    RoleResponse result = roleService.create(new LookupRequest("Security Guard"));

    assertThat(result.name()).isEqualTo("Security Guard");
    assertThat(result.id()).isEqualTo(saved.getId());
    verify(roleRepository).save(any());
  }

  // ── update ────────────────────────────────────────────────────────────────────

  @Test
  void update_changesName() {
    Role existing = role("Old Name");
    when(roleRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(roleRepository.save(existing)).thenReturn(existing);

    RoleResponse result = roleService.update(existing.getId(), new LookupRequest("New Name"));

    assertThat(result.name()).isEqualTo("New Name");
    verify(roleRepository).save(existing);
  }

  @Test
  void update_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(roleRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> roleService.update(id, new LookupRequest("X")))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── delete ────────────────────────────────────────────────────────────────────

  @Test
  void delete_callsDeleteById() {
    Role existing = role("Guard");
    when(roleRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    roleService.delete(existing.getId());

    verify(roleRepository).deleteById(existing.getId());
  }

  @Test
  void delete_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(roleRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> roleService.delete(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(roleRepository, never()).deleteById(any());
  }
}
