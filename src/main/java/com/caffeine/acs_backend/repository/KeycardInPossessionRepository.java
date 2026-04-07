package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Keycard;
import com.caffeine.acs_backend.entity.KeycardInPossession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeycardInPossessionRepository extends JpaRepository<KeycardInPossession, UUID> {

  boolean existsByKeycardAndReturnTimeIsNull(Keycard keycard);
}
