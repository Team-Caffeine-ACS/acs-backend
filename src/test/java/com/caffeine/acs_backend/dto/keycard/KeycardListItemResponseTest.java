package com.caffeine.acs_backend.dto.keycard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caffeine.acs_backend.repository.KeycardListView;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KeycardListItemResponseTest {

  @Test
  void from_mapsViewToResponseCorrectly() {
    // GIVEN
    KeycardListView view = mock(KeycardListView.class);
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    when(view.getId()).thenReturn(id);
    when(view.getKeycardNumber()).thenReturn("KC-9999");
    when(view.getStatus()).thenReturn("IN_USE");
    when(view.getAssignedUser()).thenReturn("Kalle Kaasik");

    // MUUDA SEE RIDA (või lisa see, kui sa pole kindel, kumba kood kasutab):
    when(view.getLastReturnTime()).thenReturn(now);

    // ACT
    KeycardListItemResponse response = KeycardListItemResponse.from(view);

    // ASSERT
    assertThat(response.id()).isEqualTo(id);
    assertThat(response.keycardNumber()).isEqualTo("KC-9999");
    assertThat(response.status()).isEqualTo("IN_USE");
    assertThat(response.assignedUser()).isEqualTo("Kalle Kaasik");
    assertThat(response.lastReturnTime()).isEqualTo(now);
  }

  @Test
  void from_handlesNullFields() {
    // GIVEN: Testime juhtu, kus kaart on vaba (kasutaja ja aeg on null)
    KeycardListView view = mock(KeycardListView.class);
    when(view.getStatus()).thenReturn("AVAILABLE");
    when(view.getAssignedUser()).thenReturn(null);
    when(view.getLastReturnTime()).thenReturn(null);

    // ACT
    KeycardListItemResponse response = KeycardListItemResponse.from(view);

    // ASSERT
    assertThat(response.status()).isEqualTo("AVAILABLE");
    assertThat(response.assignedUser()).isNull();
    assertThat(response.lastReturnTime()).isNull();
  }
}
