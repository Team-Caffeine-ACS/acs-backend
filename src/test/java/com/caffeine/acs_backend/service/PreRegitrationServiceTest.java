package com.caffeine.acs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.CreatePreRegistrationResponse;
import com.caffeine.acs_backend.entity.*;
import com.caffeine.acs_backend.repository.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PreRegistrationServiceTest {

  @Mock private VisitRepository visitRepository;
  @Mock private AccessPointRepository accessPointRepository;
  @Mock private UserRepository userRepository;
  @Mock private EmailService emailService;
  @Mock private PersonInRoleRepository personInRoleRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PersonRepository personRepository;

  @InjectMocks private PreRegistrationService preRegistrationService;

  @BeforeEach
  void setupSecurityContext() {
    // Simuleerime sisselogitud kasutajat, et getCurrentUser() ei viskaks viga
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("admin@test.com");
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void create_FullFlow_Success() {
    // 1. DATA SETUP
    UUID personId = UUID.randomUUID();
    UUID buildingId = UUID.randomUUID();
    LocalDateTime arrival = LocalDateTime.now().plusDays(1);

    CreatePreRegistrationRequest request =
        new CreatePreRegistrationRequest(personId, arrival, null, "Note", buildingId);

    User currentUser = new User();
    currentUser.setEmail("admin@test.com");

    Person visitorPerson = new Person();
    visitorPerson.setEmail("visitor@example.com");
    visitorPerson.setGivenName("John");
    visitorPerson.setSurname("Doe");

    AccessPoint building = new AccessPoint();
    building.setName("Main Building");

    Role visitorRole = new Role();
    visitorRole.setName("Visitor");

    // 2. MOCKING
    when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentUser));
    when(accessPointRepository.findById(buildingId)).thenReturn(Optional.of(building));
    when(personRepository.findById(personId)).thenReturn(Optional.of(visitorPerson));
    when(roleRepository.findByName("Visitor")).thenReturn(Optional.of(visitorRole));

    // Simuleerime, et PersonInRole puudub (et käivitada .orElseGet() plokk)
    when(personInRoleRepository.findByPersonAndRoleAndIsActiveTrue(any(), any()))
        .thenReturn(Optional.empty());
    when(personInRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    // Simuleerime visiidi salvestamist
    when(visitRepository.save(any()))
        .thenAnswer(
            inv -> {
              Visit v = inv.getArgument(0);
              v.setId(UUID.randomUUID()); // Anname talle ID
              return v;
            });

    // 3. ACT
    CreatePreRegistrationResponse response = preRegistrationService.create(request);

    // 4. ASSERT
    assertThat(response).isNotNull();
    assertThat(response.confirmationStatus()).isNotNull();

    // Kontrollime, et e-mail saadeti (see katab e-maili if-ploki)
    verify(emailService)
        .sendVisitorNotification(anyString(), anyString(), anyString(), anyString(), any());
    verify(visitRepository).save(any(Visit.class));
  }
}
