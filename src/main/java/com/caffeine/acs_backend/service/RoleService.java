package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.role.RoleResponse;
import com.caffeine.acs_backend.entity.Role;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.RoleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public List<RoleResponse> getAll() {
    return roleRepository.findAll().stream()
        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
        .map(RoleResponse::from)
        .toList();
  }

  @Transactional
  public RoleResponse create(LookupRequest request) {
    Role role = Role.builder().name(request.name()).build();
    return RoleResponse.from(roleRepository.save(role));
  }

  @Transactional
  public RoleResponse update(UUID id, LookupRequest request) {
    Role role = findById(id);
    role.setName(request.name());
    return RoleResponse.from(roleRepository.save(role));
  }

  @Transactional
  public void delete(UUID id) {
    findById(id);
    roleRepository.deleteById(id);
  }

  private Role findById(UUID id) {
    return roleRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Role not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
