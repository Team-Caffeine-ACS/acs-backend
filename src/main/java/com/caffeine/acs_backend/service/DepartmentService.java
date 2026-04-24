package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.department.DepartmentResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.Department;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.DepartmentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;

  public List<DepartmentResponse> getAll() {
    return departmentRepository.findAllByOrderByNameAsc().stream()
        .map(DepartmentResponse::from)
        .toList();
  }

  @Transactional
  public DepartmentResponse create(LookupRequest request) {
    Department department = Department.builder().name(request.name()).build();
    return DepartmentResponse.from(departmentRepository.save(department));
  }

  @Transactional
  public DepartmentResponse update(UUID id, LookupRequest request) {
    Department department = findById(id);
    department.setName(request.name());
    return DepartmentResponse.from(departmentRepository.save(department));
  }

  @Transactional
  public void delete(UUID id) {
    findById(id);
    departmentRepository.deleteById(id);
  }

  private Department findById(UUID id) {
    return departmentRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Department not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
