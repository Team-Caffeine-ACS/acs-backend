package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.DocumentTypeRepository;
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
class DocumentTypeServiceTest {

  @Mock private DocumentTypeRepository documentTypeRepository;
  @InjectMocks private DocumentTypeService documentTypeService;

  private DocumentType docType(String name) {
    DocumentType dt = DocumentType.builder().name(name).build();
    dt.setId(UUID.randomUUID());
    return dt;
  }

  // ── getAll ────────────────────────────────────────────────────────────────────

  @Test
  void getAll_returnsMappedList() {
    when(documentTypeRepository.findAll())
        .thenReturn(List.of(docType("Passport"), docType("ID Card")));

    List<DocumentTypeResponse> result = documentTypeService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(DocumentTypeResponse::name)
        .containsExactlyInAnyOrder("Passport", "ID Card");
  }

  @Test
  void getAll_returnsSortedAlphabetically() {
    when(documentTypeRepository.findAll())
        .thenReturn(List.of(docType("Passport"), docType("Driver License")));

    List<DocumentTypeResponse> result = documentTypeService.getAll();

    assertThat(result)
        .extracting(DocumentTypeResponse::name)
        .containsExactly("Driver License", "Passport");
  }

  @Test
  void getAll_empty_returnsEmptyList() {
    when(documentTypeRepository.findAll()).thenReturn(List.of());

    assertThat(documentTypeService.getAll()).isEmpty();
  }

  // ── create ────────────────────────────────────────────────────────────────────

  @Test
  void create_savesAndReturnsResponse() {
    DocumentType saved = docType("Passport");
    when(documentTypeRepository.save(any())).thenReturn(saved);

    DocumentTypeResponse result = documentTypeService.create(new LookupRequest("Passport"));

    assertThat(result.name()).isEqualTo("Passport");
    assertThat(result.id()).isEqualTo(saved.getId());
    verify(documentTypeRepository).save(any());
  }

  // ── update ────────────────────────────────────────────────────────────────────

  @Test
  void update_changesName() {
    DocumentType existing = docType("Old Name");
    when(documentTypeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(documentTypeRepository.save(existing)).thenReturn(existing);

    DocumentTypeResponse result =
        documentTypeService.update(existing.getId(), new LookupRequest("New Name"));

    assertThat(result.name()).isEqualTo("New Name");
    verify(documentTypeRepository).save(existing);
  }

  @Test
  void update_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(documentTypeRepository.findById(id)).thenReturn(Optional.empty());

    var request = new LookupRequest("X");
    assertThatThrownBy(() -> documentTypeService.update(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── delete ────────────────────────────────────────────────────────────────────

  @Test
  void delete_callsDeleteById() {
    DocumentType existing = docType("Passport");
    when(documentTypeRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

    documentTypeService.delete(existing.getId());

    verify(documentTypeRepository).deleteById(existing.getId());
  }

  @Test
  void delete_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(documentTypeRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> documentTypeService.delete(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(documentTypeRepository, never()).deleteById(any());
  }
}
