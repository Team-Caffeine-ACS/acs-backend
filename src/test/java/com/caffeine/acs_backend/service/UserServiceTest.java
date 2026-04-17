package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private User user(String email) {
    User u = new User();
    u.setEmail(email);
    u.setPassword("hashed");
    u.setRole(UserRole.VISITOR);
    return u;
  }

  private Person person(String givenName, String surname) {
    Person p = new Person();
    p.setGivenName(givenName);
    p.setSurname(surname);
    return p;
  }

  // ── getMe ─────────────────────────────────────────────────────────────────────

  @Test
  void getMe_returnsUserResponse() {
    User u = user("alice@example.com");
    when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

    UserResponse response = userService.getMe(u);

    assertThat(response.email()).isEqualTo("alice@example.com");
    assertThat(response.role()).isEqualTo(UserRole.VISITOR);
    assertThat(response.person()).isNull();
  }

  @Test
  void getMe_withLinkedPerson_returnsPersonDetails() {
    User u = user("alice@example.com");
    u.setPerson(person("Alice", "Smith"));
    when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

    UserResponse response = userService.getMe(u);

    assertThat(response.person()).isNotNull();
    assertThat(response.person().givenName()).isEqualTo("Alice");
    assertThat(response.person().surname()).isEqualTo("Smith");
  }

  @Test
  void getMe_userNotFound_throwsNotFound() {
    User u = user("ghost@example.com");
    when(userRepository.findById(u.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getMe(u))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  // ── updateMe ──────────────────────────────────────────────────────────────────

  @Test
  void updateMe_newEmail_updatesAndSaves() {
    User u = user("old@example.com");
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u)).thenReturn(u);

    UserResponse response = userService.updateMe(u, new UpdateUserRequest("new@example.com", null));

    assertThat(response.email()).isEqualTo("new@example.com");
    verify(userRepository).saveAndFlush(u);
  }

  @Test
  void updateMe_sameEmail_noSave() {
    User u = user("same@example.com");

    UserResponse response =
        userService.updateMe(u, new UpdateUserRequest("same@example.com", null));

    assertThat(response.email()).isEqualTo("same@example.com");
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void updateMe_duplicateEmail_throwsConflict() {
    User u = user("me@example.com");
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
    var request = new UpdateUserRequest("taken@example.com", null);

    assertThatThrownBy(() -> userService.updateMe(u, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void updateMe_newPassword_encodesAndSaves() {
    User u = user("alice@example.com");
    when(passwordEncoder.encode("newpass")).thenReturn("$2a$hashed");
    when(userRepository.saveAndFlush(u)).thenReturn(u);

    userService.updateMe(u, new UpdateUserRequest(null, "newpass"));

    assertThat(u.getPassword()).isEqualTo("$2a$hashed");
    verify(userRepository).saveAndFlush(u);
  }

  @Test
  void updateMe_nothingProvided_returnsUnchanged() {
    User u = user("alice@example.com");

    UserResponse response = userService.updateMe(u, new UpdateUserRequest(null, null));

    assertThat(response.email()).isEqualTo("alice@example.com");
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void updateMe_emailNormalisedToLowercase() {
    User u = user("old@example.com");
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u)).thenReturn(u);

    userService.updateMe(u, new UpdateUserRequest("NEW@EXAMPLE.COM", null));

    assertThat(u.getEmail()).isEqualTo("new@example.com");
  }

  // ── adminUpdateUser ───────────────────────────────────────────────────────────

  @Test
  void adminUpdateUser_userNotFound_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());
    var request = new UpdateUserRequest("a@b.com", null);

    assertThatThrownBy(() -> userService.adminUpdateUser(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void adminUpdateUser_newEmail_updatesAndSaves() {
    UUID id = UUID.randomUUID();
    User u = user("old@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u)).thenReturn(u);

    UserResponse response =
        userService.adminUpdateUser(id, new UpdateUserRequest("new@example.com", null));

    assertThat(response.email()).isEqualTo("new@example.com");
    verify(userRepository).saveAndFlush(u);
  }

  @Test
  void adminUpdateUser_newPassword_encodesAndSaves() {
    UUID id = UUID.randomUUID();
    User u = user("alice@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));
    when(passwordEncoder.encode("newpass")).thenReturn("$2a$hashed");
    when(userRepository.saveAndFlush(u)).thenReturn(u);

    userService.adminUpdateUser(id, new UpdateUserRequest(null, "newpass"));

    assertThat(u.getPassword()).isEqualTo("$2a$hashed");
    verify(userRepository).saveAndFlush(u);
  }

  @Test
  void adminUpdateUser_duplicateEmail_throwsConflict() {
    UUID id = UUID.randomUUID();
    User u = user("me@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
    var request = new UpdateUserRequest("taken@example.com", null);

    assertThatThrownBy(() -> userService.adminUpdateUser(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void adminUpdateUser_nothingProvided_returnsUnchanged() {
    UUID id = UUID.randomUUID();
    User u = user("alice@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));

    UserResponse response = userService.adminUpdateUser(id, new UpdateUserRequest(null, null));

    assertThat(response.email()).isEqualTo("alice@example.com");
    verify(userRepository, never()).saveAndFlush(any());
  }

  @Test
  void adminUpdateUser_sameEmail_noSave() {
    UUID id = UUID.randomUUID();
    User u = user("alice@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));

    UserResponse response =
        userService.adminUpdateUser(id, new UpdateUserRequest("alice@example.com", null));

    assertThat(response.email()).isEqualTo("alice@example.com");
    verify(userRepository, never()).saveAndFlush(any());
  }

  // ── DataIntegrityViolationException fallback ──────────────────────────────────

  @Test
  void updateMe_dataIntegrityViolation_throwsConflict() {
    User u = user("alice@example.com");
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    assertThatThrownBy(
            () -> userService.updateMe(u, new UpdateUserRequest("new@example.com", null)))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }

  @Test
  void adminUpdateUser_dataIntegrityViolation_throwsConflict() {
    UUID id = UUID.randomUUID();
    User u = user("alice@example.com");
    when(userRepository.findById(id)).thenReturn(Optional.of(u));
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    assertThatThrownBy(
            () -> userService.adminUpdateUser(id, new UpdateUserRequest("new@example.com", null)))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }
}
