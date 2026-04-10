package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.keycard.AssignKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.CreateKeycardRequest;
import com.caffeine.acs_backend.dto.keycard.KeycardDetailResponse;
import com.caffeine.acs_backend.dto.keycard.ReturnKeycardRequest;
import com.caffeine.acs_backend.entity.AccessPoint;
import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.entity.KeycardInPossession;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.PersonInRole;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.AccessPointRepository;
import com.caffeine.acs_backend.repository.KeycardInPossessionRepository;
import com.caffeine.acs_backend.repository.KeycardRepository;
import com.caffeine.acs_backend.repository.PersonInRoleRepository;
import java.time.LocalDateTime;
import java.util.Collections;
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
class KeycardServiceTest {

  @Mock private KeycardRepository keycardRepository;
  @Mock private KeycardInPossessionRepository keycardInPossessionRepository;
  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private AccessPointRepository accessPointRepository;

  @InjectMocks private KeycardService keycardService;

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private Keycard activeKeycard() {
    Keycard k = new Keycard();
    k.setKeycardNumber("KC-0001");
    k.setActive(true);
    return k;
  }

  private PersonInRole personInRole(String givenName, String surname) {
    Person person = new Person();
    person.setGivenName(givenName);
    person.setSurname(surname);
    PersonInRole pir = new PersonInRole();
    pir.setPerson(person);
    return pir;
  }

  private AccessPoint accessPoint() {
    return new AccessPoint();
  }

  // ── getKeycard ───────────────────────────────────────────────────────────────

  @Test
  void getKeycard_notFound_throwsBusinessException() {
    UUID id = UUID.randomUUID();
    when(keycardRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> keycardService.getKeycard(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void getKeycard_found_returnsDetail() {
    UUID id = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.findActiveByKeycard(keycard)).thenReturn(Optional.empty());
    when(keycardInPossessionRepository.findLatestByKeycard(eq(keycard), any()))
        .thenReturn(Collections.emptyList());

    KeycardDetailResponse response = keycardService.getKeycard(id);

    assertThat(response.keycardNumber()).isEqualTo("KC-0001");
    assertThat(response.assignedUser()).isNull();
  }

  // ── createKeycard ────────────────────────────────────────────────────────────

  @Test
  void createKeycard_duplicateNumber_throwsConflict() {
    when(keycardRepository.existsByKeycardNumber("KC-0001")).thenReturn(true);

    CreateKeycardRequest request = new CreateKeycardRequest("KC-0001", null, true);

    assertThatThrownBy(() -> keycardService.createKeycard(request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

    verify(keycardRepository, never()).save(any());
  }

  @Test
  void createKeycard_uniqueNumber_savesAndReturns() {
    when(keycardRepository.existsByKeycardNumber("KC-0099")).thenReturn(false);
    when(keycardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CreateKeycardRequest request = new CreateKeycardRequest("KC-0099", null, true);
    KeycardDetailResponse response = keycardService.createKeycard(request);

    assertThat(response.keycardNumber()).isEqualTo("KC-0099");
    assertThat(response.active()).isTrue();
    verify(keycardRepository).save(any(Keycard.class));
  }

  // ── assignKeycard ────────────────────────────────────────────────────────────

  @Test
  void assignKeycard_cardNotActive_throwsConflict() {
    UUID id = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    keycard.setActive(false);
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));

    AssignKeycardRequest request =
        new AssignKeycardRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    assertThatThrownBy(() -> keycardService.assignKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void assignKeycard_alreadyAssigned_throwsConflict() {
    UUID id = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard))
        .thenReturn(true);

    AssignKeycardRequest request =
        new AssignKeycardRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

    assertThatThrownBy(() -> keycardService.assignKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void assignKeycard_holderNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID holderId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard))
        .thenReturn(false);
    when(personInRoleRepository.findById(holderId)).thenReturn(Optional.empty());

    AssignKeycardRequest request =
        new AssignKeycardRequest(holderId, UUID.randomUUID(), UUID.randomUUID());

    assertThatThrownBy(() -> keycardService.assignKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void assignKeycard_assignorNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID holderId = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard))
        .thenReturn(false);
    when(personInRoleRepository.findById(holderId))
        .thenReturn(Optional.of(personInRole("Alice", "Smith")));
    when(personInRoleRepository.findById(assignorId)).thenReturn(Optional.empty());

    AssignKeycardRequest request =
        new AssignKeycardRequest(holderId, assignorId, UUID.randomUUID());

    assertThatThrownBy(() -> keycardService.assignKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void assignKeycard_accessPointNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID holderId = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard))
        .thenReturn(false);
    when(personInRoleRepository.findById(holderId))
        .thenReturn(Optional.of(personInRole("Alice", "Smith")));
    when(personInRoleRepository.findById(assignorId))
        .thenReturn(Optional.of(personInRole("Bob", "Jones")));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.empty());

    AssignKeycardRequest request = new AssignKeycardRequest(holderId, assignorId, apId);

    assertThatThrownBy(() -> keycardService.assignKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void assignKeycard_success_savesAndReturns() {
    UUID id = UUID.randomUUID();
    UUID holderId = UUID.randomUUID();
    UUID assignorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    PersonInRole holder = personInRole("Alice", "Smith");
    PersonInRole assignor = personInRole("Bob", "Jones");
    AccessPoint ap = accessPoint();

    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.existsByKeycardAndReturnTimeIsNull(keycard))
        .thenReturn(false);
    when(personInRoleRepository.findById(holderId)).thenReturn(Optional.of(holder));
    when(personInRoleRepository.findById(assignorId)).thenReturn(Optional.of(assignor));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(ap));
    when(keycardInPossessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AssignKeycardRequest request = new AssignKeycardRequest(holderId, assignorId, apId);
    KeycardDetailResponse response = keycardService.assignKeycard(id, request);

    assertThat(response.assignedUser()).isEqualTo("Alice Smith");
    assertThat(response.lastReturnTime()).isNull();
    verify(keycardInPossessionRepository).save(any(KeycardInPossession.class));
  }

  // ── returnKeycard ────────────────────────────────────────────────────────────

  @Test
  void returnKeycard_notCurrentlyAssigned_throwsConflict() {
    UUID id = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.findActiveByKeycard(keycard)).thenReturn(Optional.empty());

    ReturnKeycardRequest request = new ReturnKeycardRequest(UUID.randomUUID());

    assertThatThrownBy(() -> keycardService.returnKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void returnKeycard_returnAccessPointNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    KeycardInPossession possession =
        KeycardInPossession.builder()
            .keycard(keycard)
            .keycardHolder(personInRole("Alice", "Smith"))
            .keycardAssignor(personInRole("Bob", "Jones"))
            .assigningAccessPoint(accessPoint())
            .assignedTime(LocalDateTime.now().minusHours(1))
            .build();

    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.findActiveByKeycard(keycard))
        .thenReturn(Optional.of(possession));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.empty());

    ReturnKeycardRequest request = new ReturnKeycardRequest(apId);

    assertThatThrownBy(() -> keycardService.returnKeycard(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void returnKeycard_success_setsReturnTimeAndAccessPoint() {
    UUID id = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    Keycard keycard = activeKeycard();
    AccessPoint returnAp = accessPoint();
    KeycardInPossession possession =
        KeycardInPossession.builder()
            .keycard(keycard)
            .keycardHolder(personInRole("Alice", "Smith"))
            .keycardAssignor(personInRole("Bob", "Jones"))
            .assigningAccessPoint(accessPoint())
            .assignedTime(LocalDateTime.now().minusHours(1))
            .build();

    when(keycardRepository.findById(id)).thenReturn(Optional.of(keycard));
    when(keycardInPossessionRepository.findActiveByKeycard(keycard))
        .thenReturn(Optional.of(possession));
    when(accessPointRepository.findById(apId)).thenReturn(Optional.of(returnAp));
    when(keycardInPossessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ReturnKeycardRequest request = new ReturnKeycardRequest(apId);
    KeycardDetailResponse response = keycardService.returnKeycard(id, request);

    assertThat(possession.getReturnAccessPoint()).isEqualTo(returnAp);
    assertThat(possession.getReturnTime()).isNotNull();
    assertThat(response.assignedUser()).isNull();
    assertThat(response.lastReturnTime()).isNotNull();
    verify(keycardInPossessionRepository).save(possession);
  }
}
