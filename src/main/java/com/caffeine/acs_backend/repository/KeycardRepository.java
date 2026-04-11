package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Keycard;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeycardRepository extends JpaRepository<Keycard, UUID> {

  boolean existsByKeycardNumber(String keycardNumber);

  @Query(
      nativeQuery = true,
      value =
          "SELECT"
              + "  k.id                          AS \"id\","
              + "  k.keycard_number              AS \"keycardNumber\","
              + "  CASE"
              + "    WHEN NOT k.is_active                                         THEN 'DISABLED'"
              + "    WHEN k.valid_until IS NOT NULL AND k.valid_until < NOW()     THEN 'EXPIRED'"
              + "    WHEN kip.id IS NOT NULL                                      THEN 'IN_USE'"
              + "    ELSE 'AVAILABLE'"
              + "  END                           AS \"status\","
              + "  CASE WHEN kip.id IS NOT NULL"
              + "    THEN p.given_name || ' ' || p.surname"
              + "    ELSE NULL"
              + "  END                           AS \"assignedUser\","
              + "  (SELECT MAX(kip2.return_time)"
              + "   FROM keycard_in_possession kip2"
              + "   WHERE kip2.keycard_id = k.id) AS \"lastReturnTime\""
              + " FROM keycard k"
              + " LEFT JOIN keycard_in_possession kip"
              + "        ON kip.keycard_id = k.id AND kip.return_time IS NULL"
              + " LEFT JOIN person_in_role pir"
              + "        ON pir.id = kip.keycard_holder_person_in_role_id"
              + " LEFT JOIN person p ON p.id = pir.person_id"
              + " WHERE"
              + "   ( CAST(:search AS text) IS NULL"
              + "     OR k.keycard_number ILIKE '%' || :search || '%'"
              + "     OR (p.given_name || ' ' || p.surname) ILIKE '%' || :search || '%'"
              + "   )"
              + "   AND"
              + "   ( CAST(:status AS text) IS NULL"
              + "     OR (:status = 'disabled' AND NOT k.is_active)"
              + "     OR (:status = 'expired'  AND k.is_active"
              + "                               AND k.valid_until IS NOT NULL"
              + "                               AND k.valid_until < NOW())"
              + "     OR (:status = 'in_use'   AND kip.id IS NOT NULL)"
              + "     OR (:status = 'available' AND k.is_active"
              + "                                AND (k.valid_until IS NULL OR k.valid_until >= NOW())"
              + "                                AND kip.id IS NULL)"
              + "   )"
              + " ORDER BY k.keycard_number",
      countQuery =
          "SELECT COUNT(*)"
              + " FROM keycard k"
              + " LEFT JOIN keycard_in_possession kip"
              + "        ON kip.keycard_id = k.id AND kip.return_time IS NULL"
              + " LEFT JOIN person_in_role pir"
              + "        ON pir.id = kip.keycard_holder_person_in_role_id"
              + " LEFT JOIN person p ON p.id = pir.person_id"
              + " WHERE"
              + "   ( CAST(:search AS text) IS NULL"
              + "     OR k.keycard_number ILIKE '%' || :search || '%'"
              + "     OR (p.given_name || ' ' || p.surname) ILIKE '%' || :search || '%'"
              + "   )"
              + "   AND"
              + "   ( CAST(:status AS text) IS NULL"
              + "     OR (:status = 'disabled' AND NOT k.is_active)"
              + "     OR (:status = 'expired'  AND k.is_active"
              + "                               AND k.valid_until IS NOT NULL"
              + "                               AND k.valid_until < NOW())"
              + "     OR (:status = 'in_use'   AND kip.id IS NOT NULL)"
              + "     OR (:status = 'available' AND k.is_active"
              + "                                AND (k.valid_until IS NULL OR k.valid_until >= NOW())"
              + "                                AND kip.id IS NULL)"
              + "   )")
  Page<KeycardListView> findAllFiltered(
      @Param("search") String search, @Param("status") String status, Pageable pageable);
}
