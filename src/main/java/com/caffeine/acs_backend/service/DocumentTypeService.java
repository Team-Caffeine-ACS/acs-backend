package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.repository.DocumentTypeRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
