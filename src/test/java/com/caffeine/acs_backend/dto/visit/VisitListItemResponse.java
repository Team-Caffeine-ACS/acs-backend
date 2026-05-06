package com.caffeine.acs_backend.dto.visit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitListView;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VisitListItemResponseTest {

  @Test
  void from_withValidStatus_mapsCorrectally() {
    // GIVEN
    VisitListView view = mock(VisitListView.class);
    UUID id = UUID.randomUUID();
    when(view.getId()).thenReturn(id);
    when(view.getFullName()).thenReturn("John Smith");
    when(view.getVisitStatus()).thenReturn("  in_building  "); // Testime trimimist ja toUpperCase

    // ACT
    VisitListItemResponse response = VisitListItemResponse.from(view);

    // ASSERT
    assertThat(response.id()).isEqualTo(id);
    assertThat(response.fullName()).isEqualTo("John Smith");
    assertThat(response.status()).isEqualTo(VisitStatus.IN_BUILDING);
  }

  @Test
  void from_withInvalidStatus_logsErrorAndReturnsNullStatus() {
    // GIVEN
    VisitListView view = mock(VisitListView.class);
    when(view.getId()).thenReturn(UUID.randomUUID());
    when(view.getVisitStatus()).thenReturn("UNKNOWN_STATE_123");

    // ACT
    VisitListItemResponse response = VisitListItemResponse.from(view);

    // ASSERT
    // Catch plokk käivitatakse, status jääb nulliks, aga objekt luuakse lõpuni
    assertThat(response.status()).isNull();
  }

  @Test
  void from_withNullStatus_returnsNullStatus() {
    // GIVEN
    VisitListView view = mock(VisitListView.class);
    when(view.getVisitStatus()).thenReturn(null);

    // ACT
    VisitListItemResponse response = VisitListItemResponse.from(view);

    // ASSERT
    assertThat(response.status()).isNull();
  }

  @Test
  void from_withLegacyStatus_mapsToPlanned() {
    VisitListView view = mock(VisitListView.class);
    when(view.getVisitStatus()).thenReturn(" pre_registered ");

    VisitListItemResponse response = VisitListItemResponse.from(view);

    assertThat(response.status()).isEqualTo(VisitStatus.PLANNED);
  }

  @Test
  void from_mapsAllFieldsCorrectally() {
    // GIVEN
    VisitListView view = mock(VisitListView.class);
    UUID id = UUID.randomUUID();
    UUID visitorId = UUID.randomUUID();
    UUID apId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    when(view.getId()).thenReturn(id);
    when(view.getFullName()).thenReturn("Name");
    when(view.getDocumentNumber()).thenReturn("AB123");
    when(view.getOrganizationName()).thenReturn("Org");
    when(view.getHostName()).thenReturn("Host");
    when(view.getEntryTime()).thenReturn(now);
    when(view.getExitTime()).thenReturn(now.plusHours(1));
    when(view.getVisitorId()).thenReturn(visitorId);
    when(view.getAccessPointId()).thenReturn(apId);
    when(view.getAccessPointName()).thenReturn("Main Gate");
    when(view.getAccessPointAddress()).thenReturn("Road 1");

    // ACT
    VisitListItemResponse response = VisitListItemResponse.from(view);

    // ASSERT
    assertThat(response.documentNumber()).isEqualTo("AB123");
    assertThat(response.organizationName()).isEqualTo("Org");
    assertThat(response.hostName()).isEqualTo("Host");
    assertThat(response.entryTime()).isEqualTo(now);
    assertThat(response.exitTime()).isEqualTo(now.plusHours(1));
    assertThat(response.visitorId()).isEqualTo(visitorId);
    assertThat(response.accessPointId()).isEqualTo(apId);
    assertThat(response.accessPointName()).isEqualTo("Main Gate");
    assertThat(response.accessPointAddress()).isEqualTo("Road 1");
  }
}
