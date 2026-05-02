package com.caffeine.acs_backend.dto.keycard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.repository.KeycardListView;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KeycardResponseTest {

  @Test
  void from_mapsEntityToResponseCorrectly() {
    // GIVEN
    Keycard keycard = mock(Keycard.class);
    UUID id = UUID.randomUUID();
    LocalDateTime expiry = LocalDateTime.now().plusYears(1);
    String cardNumber = "CARD-12345";

    when(keycard.getId()).thenReturn(id);
    when(keycard.getKeycardNumber()).thenReturn(cardNumber);
    when(keycard.getValidUntil()).thenReturn(expiry);

    // ACT
    KeycardResponse response = KeycardResponse.from(keycard);

    // ASSERT
    assertThat(response.id()).isEqualTo(id);
    assertThat(response.keycardNumber()).isEqualTo(cardNumber);
    assertThat(response.validUntil()).isEqualTo(expiry);
  }

  @Test
  void record_accessorMethodsWork() {
    // See test tagab, et recordi enda konstruktor ja accessorid töötavad
    UUID id = UUID.randomUUID();
    KeycardResponse response = new KeycardResponse(id, "TEST-1", null);

    assertThat(response.id()).isEqualTo(id);
    assertThat(response.keycardNumber()).isEqualTo("TEST-1");
    assertThat(response.validUntil()).isNull();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private Keycard keycard() {
    Keycard k = Keycard.builder().keycardNumber("KC-0001").isActive(true).build();
    k.setId(UUID.randomUUID());
    return k;
  }

  private KeycardListView listView(LocalDateTime lastReturnTime) {
    KeycardListView view = mock(KeycardListView.class);
    when(view.getId()).thenReturn(UUID.randomUUID());
    when(view.getKeycardNumber()).thenReturn("KC-0001");
    when(view.getStatus()).thenReturn("AVAILABLE");
    when(view.getAssignedUser()).thenReturn(null);
    when(view.getLastReturnTime()).thenReturn(lastReturnTime);
    return view;
  }

  // ── KeycardDetailResponse.from — lastReturnTime filtering ─────────────────────

  @Test
  void detailResponse_pastReturnTime_isPreserved() {
    LocalDateTime past = LocalDateTime.now().minusHours(1);

    KeycardDetailResponse response = KeycardDetailResponse.from(keycard(), null, past);

    assertThat(response.lastReturnTime()).isEqualTo(past);
  }

  @Test
  void detailResponse_nullReturnTime_remainsNull() {
    KeycardDetailResponse response = KeycardDetailResponse.from(keycard(), null, null);

    assertThat(response.lastReturnTime()).isNull();
  }

  // ── KeycardListItemResponse.from — lastReturnTime filtering ───────────────────

  @Test
  void listResponse_pastReturnTime_isPreserved() {
    LocalDateTime past = LocalDateTime.now().minusDays(1);

    KeycardListItemResponse response = KeycardListItemResponse.from(listView(past));

    assertThat(response.lastReturnTime()).isEqualTo(past);
  }

  @Test
  void listResponse_nullReturnTime_remainsNull() {
    KeycardListItemResponse response = KeycardListItemResponse.from(listView(null));

    assertThat(response.lastReturnTime()).isNull();
  }
}
