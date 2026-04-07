package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.person.CreatePersonRequest;
import com.caffeine.acs_backend.dto.person.PersonInRoleResponse;
import com.caffeine.acs_backend.dto.person.PersonResponse;
import com.caffeine.acs_backend.entity.Document;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.entity.Nationality;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.repository.DocumentTypeRepository;
import com.caffeine.acs_backend.repository.NationalityRepository;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import com.caffeine.acs_backend.repository.PersonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {

  private final PersonInRoleRepository personInRoleRepository;
  private final PersonRepository personRepository;
  private final NationalityRepository nationalityRepository;
  private final DocumentTypeRepository documentTypeRepository;

  @Transactional
  public PersonResponse createPerson(CreatePersonRequest request) {
    Nationality nationality =
        nationalityRepository
            .findById(request.nationalityId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Nationality not found: " + request.nationalityId()));

    Person person =
        Person.builder()
            .givenName(request.givenName().trim())
            .surname(request.surname().trim())
            .nationality(nationality)
            .build();
    personRepository.save(person);

    boolean hasDocumentNumber =
        request.documentNumber() != null && !request.documentNumber().isBlank();
    boolean hasDocumentTypeId = request.documentTypeId() != null;
    if (hasDocumentNumber != hasDocumentTypeId) {
      throw new IllegalArgumentException(
          "documentNumber and documentTypeId must both be provided together");
    }
    if (hasDocumentNumber) {
      DocumentType documentType =
          documentTypeRepository
              .findById(request.documentTypeId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Document type not found: " + request.documentTypeId()));
      person
          .getDocuments()
          .add(
              Document.builder()
                  .documentNumber(request.documentNumber())
                  .documentType(documentType)
                  .person(person)
                  .build());
      personRepository.save(person);
    }

    return PersonResponse.from(person);
  }

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
