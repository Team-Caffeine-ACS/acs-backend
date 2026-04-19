package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.DocumentTypeRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {

  private final DocumentTypeRepository documentTypeRepository;

  public List<DocumentTypeResponse> getAll() {
    return documentTypeRepository.findAll().stream()
        .sorted(Comparator.comparing(DocumentType::getName))
        .map(DocumentTypeResponse::from)
        .toList();
  }

  @Transactional
  public DocumentTypeResponse create(LookupRequest request) {
    DocumentType documentType = DocumentType.builder().name(request.name()).build();
    return DocumentTypeResponse.from(documentTypeRepository.save(documentType));
  }

  @Transactional
  public DocumentTypeResponse update(UUID id, LookupRequest request) {
    DocumentType documentType = findById(id);
    documentType.setName(request.name());
    return DocumentTypeResponse.from(documentTypeRepository.save(documentType));
  }

  @Transactional
  public void delete(UUID id) {
    findById(id);
    documentTypeRepository.deleteById(id);
  }

  private DocumentType findById(UUID id) {
    return documentTypeRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Document type not found", ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
