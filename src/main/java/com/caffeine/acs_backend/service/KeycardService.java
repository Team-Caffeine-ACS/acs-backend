package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.keycard.AssignKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.CreateKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.KeycardDetailResponse;
import com.caffeine.acs_backend.dto.keycard.KeycardListItemResponse;
import com.caffeine.acs_backend.dto.keycard.ReturnKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.UpdateKeycardRequest;
import com.caffeine.acs_backend.entity.AccessPoint;
import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.entity.KeycardInPossession;
import com.caffeine.acs_backend.entity.PersonInRole;
import com.caffeine.acs_backend.enums.errorcode.ErrorCode;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.KeycardInPossessionRepository;
import com.caffeine.acs_backend.repository.KeycardRepository;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KeycardService {

  private final KeycardRepository keycardRepository;
  private final KeycardInPossessionRepository keycardInPossessionRepository;
  private final PersonInRoleRepository personInRoleRepository;
  private final AccessPointRepository accessPointRepository;

  @Transactional(readOnly = true)
  public Page<KeycardListItemResponse> getKeycards(
      String search, String status, Pageable pageable) {
    String searchParam = (search == null || search.isBlank()) ? null : search.trim();
    String statusParam = (status == null || status.isBlank()) ? null : status.trim().toLowerCase();
    return keycardRepository
        .findAllFiltered(searchParam, statusParam, pageable)
        .map(KeycardListItemResponse::from);
  }

  @Transactional(readOnly = true)
  public KeycardDetailResponse getKeycard(UUID id) {
    Keycard keycard = findKeycardOrThrow(id);
    KeycardInPossession active =
        keycardInPossessionRepository.findActiveByKeycard(keycard).orElse(null);
    LocalDateTime lastReturn = null;
    if (active == null) {
      lastReturn =
          keycardInPossessionRepository
              .findLatestByKeycard(keycard, PageRequest.of(0, 1))
              .stream()
              .findFirst()
              .map(KeycardInPossession::getReturnTime)
              .orElse(null);
    }
    return KeycardDetailResponse.from(keycard, active, lastReturn);
  }

  @Transactional
  public KeycardDetailResponse createKeycard(CreateKeycardRequest request) {
    if (keycardRepository.existsByKeycardNumber(request.keycardNumber())) {
      throw new BusinessException(
          "Keycard number already exists: " + request.keycardNumber(),
          ErrorCode.RESOURCE_ALREADY_EXISTS,
          HttpStatus.CONFLICT);
    }
    Keycard keycard =
        Keycard.builder()
            .keycardNumber(request.keycardNumber())
            .validUntil(request.validUntil())
            .isActive(request.initialStatus())
            .build();
    keycardRepository.save(keycard);
    return KeycardDetailResponse.from(keycard, null, null);
  }

  @Transactional
  public KeycardDetailResponse updateKeycard(UUID id, UpdateKeycardRequest request) {
    Keycard keycard = findKeycardOrThrow(id);
    if (request.keycardNumber() != null && !request.keycardNumber().isBlank()) {
      keycard.setKeycardNumber(request.keycardNumber());
    }
    if (request.active() != null) {
      keycard.setActive(request.active());
    }
    if (request.validUntil() != null) {
      keycard.setValidUntil(request.validUntil());
    }
    keycardRepository.save(keycard);
    KeycardInPossession active =
        keycardInPossessionRepository.findActiveByKeycard(keycard).orElse(null);
    LocalDateTime lastReturn = null;
    if (active == null) {
      lastReturn =
          keycardInPossessionRepository
              .findLatestByKeycard(keycard, PageRequest.of(0, 1))
              .stream()
              .findFirst()
              .map(KeycardInPossession::getReturnTime)
              .orElse(null);
    }
    return KeycardDetailResponse.from(keycard, active, lastReturn);
  }

  @Transactional
  public KeycardDetailResponse assignKeycard(UUID id, AssignKeycardRequest request) {
    Keycard keycard = findKeycardOrThrow(id);

    if (!keycard.isActive()) {
      throw new BusinessException(
          "Keycard is not active", ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.CONFLICT);
    }
    if (keycard.getValidUntil() != null && keycard.getValidUntil().isBefore(LocalDateTime.now())) {
      throw new BusinessException(
          "Keycard is expired", ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.CONFLICT);
    }
    if (keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard)) {
      throw new BusinessException(
          "Keycard is already assigned", ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.CONFLICT);
    }

    PersonInRole holder =
        personInRoleRepository
            .findById(request.keycardHolderPersonInRoleId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Holder PersonInRole not found: " + request.keycardHolderPersonInRoleId(),
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

    PersonInRole assignor =
        personInRoleRepository
            .findById(request.keycardAssignorPersonInRoleId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Assignor PersonInRole not found: "
                            + request.keycardAssignorPersonInRoleId(),
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

    AccessPoint accessPoint =
        accessPointRepository
            .findById(request.assigningAccessPointId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Access point not found: " + request.assigningAccessPointId(),
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

    KeycardInPossession possession =
        KeycardInPossession.builder()
            .keycard(keycard)
            .keycardHolder(holder)
            .keycardAssignor(assignor)
            .assigningAccessPoint(accessPoint)
            .assignedTime(LocalDateTime.now())
            .build();
    keycardInPossessionRepository.save(possession);

    return KeycardDetailResponse.from(keycard, possession, null);
  }

  @Transactional
  public KeycardDetailResponse returnKeycard(UUID id, ReturnKeycardRequest request) {
    Keycard keycard = findKeycardOrThrow(id);

    KeycardInPossession possession =
        keycardInPossessionRepository
            .findActiveByKeycard(keycard)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Keycard is not currently assigned",
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        HttpStatus.CONFLICT));

    AccessPoint returnPoint =
        accessPointRepository
            .findById(request.returnAccessPointId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Access point not found: " + request.returnAccessPointId(),
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

    possession.setReturnAccessPoint(returnPoint);
    possession.setReturnTime(LocalDateTime.now());
    keycardInPossessionRepository.save(possession);

    return KeycardDetailResponse.from(keycard, null, possession.getReturnTime());
  }

  private Keycard findKeycardOrThrow(UUID id) {
    return keycardRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BusinessException(
                    "Keycard not found: " + id,
                    ErrorCode.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND));
  }
}
