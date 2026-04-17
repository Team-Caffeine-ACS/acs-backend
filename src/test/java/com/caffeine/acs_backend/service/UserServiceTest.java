package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.user.AdminCreateUserRequest;
import com.caffeine.acs_backend.dto.user.UpdateUserRequest;
import com.caffeine.acs_backend.dto.user.UserResponse;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import com.caffeine.acs_backend.exception.BusinessException;
import com.caffeine.acs_backend.repository.UserRepository;
import java.util.List;
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

  // ── adminGetAllUsers ──────────────────────────────────────────────────────────

  @Test
  void adminGetAllUsers_returnsAllUsers() {
    User u1 = user("alice@example.com");
    User u2 = user("bob@example.com");
    when(userRepository.findAll()).thenReturn(List.of(u1, u2));

    List<UserResponse> result = userService.adminGetAllUsers();

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(UserResponse::email)
        .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
  }

  @Test
  void adminGetAllUsers_emptyRepository_returnsEmptyList() {
    when(userRepository.findAll()).thenReturn(List.of());

    List<UserResponse> result = userService.adminGetAllUsers();

    assertThat(result).isEmpty();
  }

  // ── adminCreateUser ───────────────────────────────────────────────────────────

  @Test
  void adminCreateUser_success_returnsCreatedUser() {
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password1!")).thenReturn("$2a$hashed");
    User saved = user("new@example.com");
    saved.setRole(UserRole.RECEPTIONIST);
    when(userRepository.save(any())).thenReturn(saved);

    UserResponse result =
        userService.adminCreateUser(
            new AdminCreateUserRequest("new@example.com", "Password1!", UserRole.RECEPTIONIST));

    assertThat(result.email()).isEqualTo("new@example.com");
    assertThat(result.role()).isEqualTo(UserRole.RECEPTIONIST);
    verify(userRepository).save(any());
  }

  @Test
  void adminCreateUser_nullRole_defaultsToVisitor() {
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
    User saved = user("new@example.com");
    when(userRepository.save(any())).thenReturn(saved);

    UserResponse result =
        userService.adminCreateUser(
            new AdminCreateUserRequest("new@example.com", "Password1!", null));

    assertThat(result.role()).isEqualTo(UserRole.VISITOR);
  }

  @Test
  void adminCreateUser_emailNormalisedToLowercase() {
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("$2a$hashed");
    User saved = user("new@example.com");
    when(userRepository.save(any())).thenReturn(saved);

    userService.adminCreateUser(new AdminCreateUserRequest("NEW@EXAMPLE.COM", "Password1!", null));

    verify(userRepository).existsByEmail("new@example.com");
  }

  @Test
  void adminCreateUser_duplicateEmail_throwsConflict() {
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
    var request = new AdminCreateUserRequest("taken@example.com", "Password1!", null);

    assertThatThrownBy(() -> userService.adminCreateUser(request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

    verify(userRepository, never()).save(any());
  }

  @Test
  void adminCreateUser_encodesPassword() {
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password1!")).thenReturn("$2a$encoded");
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    userService.adminCreateUser(new AdminCreateUserRequest("new@example.com", "Password1!", null));

    verify(passwordEncoder).encode("Password1!");
  }

  // ── adminDeleteUser ───────────────────────────────────────────────────────────

  @Test
  void adminDeleteUser_existingId_deletesUser() {
    UUID id = UUID.randomUUID();
    when(userRepository.existsById(id)).thenReturn(true);

    userService.adminDeleteUser(id);

    verify(userRepository).deleteById(id);
  }

  @Test
  void adminDeleteUser_unknownId_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(userRepository.existsById(id)).thenReturn(false);

    assertThatThrownBy(() -> userService.adminDeleteUser(id))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

    verify(userRepository, never()).deleteById(any());
  }

  // ── DataIntegrityViolationException fallback ──────────────────────────────────

  @Test
  void updateMe_dataIntegrityViolation_throwsConflict() {
    User u = user("alice@example.com");
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(u))
        .thenThrow(new DataIntegrityViolationException("duplicate"));
    var request = new UpdateUserRequest("new@example.com", null);

    assertThatThrownBy(() -> userService.updateMe(u, request))
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
    var request = new UpdateUserRequest("new@example.com", null);

    assertThatThrownBy(() -> userService.adminUpdateUser(id, request))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
  }
}
