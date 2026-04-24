package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.organization.OrganizationResponse;
import com.caffeine.acs_backend.entity.Organization;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  public List<OrganizationResponse> getAll() {
    return organizationRepository.findAllByOrderByNameAsc().stream()
        .map(OrganizationResponse::from)
        .toList();
  }

  @Transactional
  public OrganizationResponse create(LookupRequest request) {
    Organization organization = Organization.builder().name(request.name()).build();
    return OrganizationResponse.from(organizationRepository.save(organization));
  }

  @Transactional
  public OrganizationResponse update(UUID id, LookupRequest request) {
    Organization organization = findById(id);
    organization.setName(request.name());
    return OrganizationResponse.from(organizationRepository.save(organization));
  }

  @Transactional
  public void delete(UUID id) {
    findById(id);
    organizationRepository.deleteById(id);
  }

  private Organization findById(UUID id) {
    return organizationRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Organization not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
