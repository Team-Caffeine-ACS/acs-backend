package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.person.PersonInRoleResponse;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonInRoleRepository personInRoleRepository;

  public List<PersonInRoleResponse> search(String query, String role) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    String roleFilter = (role == null || role.isBlank()) ? null : role.trim();
    return personInRoleRepository.searchByPersonName(query.trim(), roleFilter).stream()
        .map(PersonInRoleResponse::from)
        .toList();
  }
}
