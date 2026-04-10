package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.entity.KeycardInPossession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeycardInPossessionRepository extends JpaRepository<KeycardInPossession, UUID> {

  boolean existsByKeycardAndReturnTimeIsNull(Keycard keycard);

  @Query(
      "SELECT kp FROM KeycardInPossession kp"
          + " JOIN FETCH kp.keycardHolder holder"
          + " JOIN FETCH holder.person"
          + " WHERE kp.keycard = :keycard AND kp.returnTime IS NULL")
  Optional<KeycardInPossession> findActiveByKeycard(@Param("keycard") Keycard keycard);

  @Query(
      "SELECT kp FROM KeycardInPossession kp"
          + " JOIN FETCH kp.keycardHolder holder"
          + " JOIN FETCH holder.person"
          + " WHERE kp.keycard = :keycard"
          + " ORDER BY kp.assignedTime DESC")
  java.util.List<KeycardInPossession> findLatestByKeycard(
      @Param("keycard") Keycard keycard, Pageable pageable);
}
