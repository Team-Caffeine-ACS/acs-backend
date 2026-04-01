package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Keycard;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeycardRepository extends JpaRepository<Keycard, UUID> {

  @Query(
      "SELECT k FROM Keycard k WHERE k.isActive = true"
          + " AND (k.validUntil IS NULL OR k.validUntil > CURRENT_TIMESTAMP)"
          + " AND NOT EXISTS ("
          + "   SELECT 1 FROM KeycardInPossession kp"
          + "   WHERE kp.keycard = k AND kp.returnTime IS NULL"
          + " )")
  List<Keycard> findAvailableKeycards();
}
