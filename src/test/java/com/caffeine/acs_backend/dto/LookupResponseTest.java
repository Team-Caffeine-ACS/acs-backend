package com.caffeine.acs_backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.dto.country.CountryResponse;
import com.caffeine.acs_backend.dto.department.DepartmentResponse;
import com.caffeine.acs_backend.dto.documenttype.DocumentTypeResponse;
import com.caffeine.acs_backend.dto.lookup.LookupRequest;
import com.caffeine.acs_backend.dto.organization.OrganizationResponse;
import com.caffeine.acs_backend.dto.role.RoleResponse;
import com.caffeine.acs_backend.entity.Country;
import com.caffeine.acs_backend.entity.Department;
import com.caffeine.acs_backend.entity.DocumentType;
import com.caffeine.acs_backend.entity.Organization;
import com.caffeine.acs_backend.entity.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LookupResponseTest {

  private static final Validator VALIDATOR =
      Validation.buildDefaultValidatorFactory().getValidator();

  // ── LookupRequest validation ──────────────────────────────────────────────────

  @Test
  void lookupRequest_validName_passesValidation() {
    assertThat(VALIDATOR.validate(new LookupRequest("Engineering"))).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void lookupRequest_blankName_failsValidation(String name) {
    assertThat(VALIDATOR.validate(new LookupRequest(name))).isNotEmpty();
  }

  @Test
  void lookupRequest_nullName_failsValidation() {
    assertThat(VALIDATOR.validate(new LookupRequest(null))).isNotEmpty();
  }

  // ── CountryResponse ───────────────────────────────────────────────────────────

  @Test
  void countryResponse_from_mapsIdAndName() {
    Country c = Country.builder().name("Estonia").build();
    c.setId(UUID.randomUUID());

    CountryResponse r = CountryResponse.from(c);

    assertThat(r.id()).isEqualTo(c.getId());
    assertThat(r.name()).isEqualTo("Estonia");
  }

  // ── DepartmentResponse ────────────────────────────────────────────────────────

  @Test
  void departmentResponse_from_mapsIdAndName() {
    Department d = Department.builder().name("Engineering").build();
    d.setId(UUID.randomUUID());

    DepartmentResponse r = DepartmentResponse.from(d);

    assertThat(r.id()).isEqualTo(d.getId());
    assertThat(r.name()).isEqualTo("Engineering");
  }

  // ── OrganizationResponse ──────────────────────────────────────────────────────

  @Test
  void organizationResponse_from_mapsIdAndName() {
    Organization o = Organization.builder().name("Acme Corp").build();
    o.setId(UUID.randomUUID());

    OrganizationResponse r = OrganizationResponse.from(o);

    assertThat(r.id()).isEqualTo(o.getId());
    assertThat(r.name()).isEqualTo("Acme Corp");
  }

  // ── RoleResponse ──────────────────────────────────────────────────────────────

  @Test
  void roleResponse_from_mapsIdAndName() {
    Role role = Role.builder().name("Security Guard").build();
    role.setId(UUID.randomUUID());

    RoleResponse r = RoleResponse.from(role);

    assertThat(r.id()).isEqualTo(role.getId());
    assertThat(r.name()).isEqualTo("Security Guard");
  }

  // ── DocumentTypeResponse ──────────────────────────────────────────────────────

  @Test
  void documentTypeResponse_from_mapsIdAndName() {
    DocumentType dt = DocumentType.builder().name("Passport").build();
    dt.setId(UUID.randomUUID());

    DocumentTypeResponse r = DocumentTypeResponse.from(dt);

    assertThat(r.id()).isEqualTo(dt.getId());
    assertThat(r.name()).isEqualTo("Passport");
  }
}
