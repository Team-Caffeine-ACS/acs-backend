package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.department.DepartmentResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.Department;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.DepartmentRepository;
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
class DepartmentServiceTest {

  @Mock private DepartmentRepository departmentRepository;
  @InjectMocks private DepartmentService departmentService;

  private Department department(String name) {
    Department d = Department.builder().name(name).build();
    d.setId(UUID.randomUUID());
    return d;
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsMappedList() {
    when(departmentRepository.findAllByOrderByNameAsc())
        .thenReturn(List.of(department("Engineering"), department("HR")));

    List<DepartmentResponse> result = departmentService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(DepartmentResponse::name).containsExactly("Engineering", "HR");
  }

  @Test
  void getAll_empty_returnsEmptyList() {
    when(departmentRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    assertThat(departmentService.getAll()).isEmpty();
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_savesAndReturnsResponse() {
    Department saved = department("Engineering");
    when(departmentRepository.save(any())).thenReturn(saved);

    DepartmentResponse result = departmentService.create(new LookupRequest("Engineering"));

    assertThat(result.name()).isEqualTo("Engineering");
    assertThat(result.id()).isEqualTo(saved.getId());
    verify(departmentRepository).save(any());
  }

  // ── update ────────────────────────────────────────────────────────────────────

  @Test
  void update_changesName() {
    Department existing = department("Old Name");
    when(departmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(departmentRepository.save(existing)).thenReturn(existing);

    DepartmentResponse result =
        departmentService.update(existing.getId(), new LookupRequest("New Name"));

    assertThat(result.name()).isEqualTo("New Name");
    verify(departmentRepository).save(existing);
  }

  @Test
  void update_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(departmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> departmentService.update(id, new LookupRequest("X")))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── delete ────────────────────────────────────────────────────────────────────

  @Test
  void delete_callsDeleteById() {
    Department existing = department("Engineering");
    when(departmentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    departmentService.delete(existing.getId());

    verify(departmentRepository).deleteById(existing.getId());
  }

  @Test
  void delete_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(departmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> departmentService.delete(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(departmentRepository, never()).deleteById(any());
  }
}
