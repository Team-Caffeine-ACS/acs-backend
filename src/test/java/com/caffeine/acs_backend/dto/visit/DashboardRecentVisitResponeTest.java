package com.caffeine.acs_backend.dto.visit;

import static org.assertj.core.api.Assertions.assertThat;

import com.caffeine.acs_backend.enums.VisitStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardRecentVisitResponseTest {

  @Test
  void record_storesAndReturnsValues() {
    // GIVEN
    UUID visitorId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    String fullName = "Jaan Tamm";
    String org = "Caffeine OÜ";
    String apName = "Peasissepääs";
    String apAddr = "Laki 12";
    VisitStatus status = VisitStatus.IN_BUILDING;

    // ACT
    DashboardRecentVisitResponse response =
        new DashboardRecentVisitResponse(
            fullName, org, now, now.plusHours(2), status, visitorId, apName, apAddr);

    // ASSERT
    // Kuna see on record, siis loeme andmeid ilma "get" eesliiteta
    assertThat(response.fullName()).isEqualTo(fullName);
    assertThat(response.organization()).isEqualTo(org);
    assertThat(response.entryTime()).isEqualTo(now);
    assertThat(response.exitTime()).isEqualTo(now.plusHours(2));
    assertThat(response.status()).isEqualTo(status);
    assertThat(response.visitorId()).isEqualTo(visitorId);
    assertThat(response.accessPointName()).isEqualTo(apName);
    assertThat(response.accessPointAddress()).isEqualTo(apAddr);
  }
}
