package com.caffeine.acs_backend.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.entity.Department;
import com.caffeine.acs_backend.entity.Organization;
import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.User;
import com.caffeine.acs_backend.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserResponseTest {

  private User userWithoutPerson() {
    User u = new User();
    u.setEmail("alice@example.com");
    u.setRole(UserRole.VISITOR);
    return u;
  }

  private Person fullPerson() {
    Department dept = new Department();
    dept.setName("Engineering");

    Organization org = new Organization();
    org.setName("Acme Corp");

    Person p = new Person();
    p.setId(UUID.randomUUID());
    p.setGivenName("Alice");
    p.setSurname("Smith");
    p.setJobTitle("Engineer");
    p.setSocialSecurityNumber("38001085718");
    p.setDepartment(dept);
    p.setOrganization(org);
    return p;
  }

  // ── UserResponse.from ─────────────────────────────────────────────────────────

  @Test
  void from_userWithoutPerson_personFieldsAreNull() {
    UserResponse response = UserResponse.from(userWithoutPerson());

    assertThat(response.email()).isEqualTo("alice@example.com");
    assertThat(response.role()).isEqualTo(UserRole.VISITOR);
    assertThat(response.personId()).isNull();
    assertThat(response.person()).isNull();
  }

  @Test
  void from_userWithPerson_populatesPersonDetails() {
    User u = userWithoutPerson();
    u.setPerson(fullPerson());

    UserResponse response = UserResponse.from(u);

    assertThat(response.personId()).isNotNull();
    assertThat(response.person()).isNotNull();
    assertThat(response.person().givenName()).isEqualTo("Alice");
    assertThat(response.person().surname()).isEqualTo("Smith");
    assertThat(response.person().jobTitle()).isEqualTo("Engineer");
    assertThat(response.person().socialSecurityNumber()).isEqualTo("38001085718");
    assertThat(response.person().department()).isEqualTo("Engineering");
    assertThat(response.person().organization()).isEqualTo("Acme Corp");
  }

  // ── PersonDetails.from ────────────────────────────────────────────────────────

  @Test
  void personDetailsFrom_allFields_mapsCorrectly() {
    UserResponse.PersonDetails details = UserResponse.PersonDetails.from(fullPerson());

    assertThat(details.givenName()).isEqualTo("Alice");
    assertThat(details.surname()).isEqualTo("Smith");
    assertThat(details.jobTitle()).isEqualTo("Engineer");
    assertThat(details.socialSecurityNumber()).isEqualTo("38001085718");
    assertThat(details.department()).isEqualTo("Engineering");
    assertThat(details.organization()).isEqualTo("Acme Corp");
  }

  @Test
  void personDetailsFrom_nullDepartment_departmentIsNull() {
    Person p = fullPerson();
    p.setDepartment(null);

    UserResponse.PersonDetails details = UserResponse.PersonDetails.from(p);

    assertThat(details.department()).isNull();
    assertThat(details.organization()).isEqualTo("Acme Corp");
  }

  @Test
  void personDetailsFrom_nullOrganization_organizationIsNull() {
    Person p = fullPerson();
    p.setOrganization(null);

    UserResponse.PersonDetails details = UserResponse.PersonDetails.from(p);

    assertThat(details.organization()).isNull();
    assertThat(details.department()).isEqualTo("Engineering");
  }

  @Test
  void personDetailsFrom_nullOptionalFields_returnsNulls() {
    Person p = new Person();
    p.setGivenName("Bob");
    p.setSurname("Jones");

    UserResponse.PersonDetails details = UserResponse.PersonDetails.from(p);

    assertThat(details.givenName()).isEqualTo("Bob");
    assertThat(details.surname()).isEqualTo("Jones");
    assertThat(details.jobTitle()).isNull();
    assertThat(details.socialSecurityNumber()).isNull();
    assertThat(details.department()).isNull();
    assertThat(details.organization()).isNull();
  }
}
