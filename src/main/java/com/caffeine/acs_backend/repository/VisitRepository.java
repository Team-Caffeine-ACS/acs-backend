package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Visit;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitRepository extends JpaRepository<Visit, UUID> {

  @Query(
      value =
          """
        SELECT v.id, v.access_point_id, v.arrival_time,
            v.assignor_person_in_role_id, v.comment, v.escort_person_in_role_id,
            v.exit_time, v.group_in_visit_id, v.host_person_in_role_id,
            v.notes, v.status, v.visitor_person_in_role_id, v.created_at
        FROM visit v
        WHERE v.status = 'PRE_REGISTERED'
        AND (CAST(:status AS varchar) IS NULL OR v.status = CAST(:status AS varchar))
        AND (CAST(:buildingId AS uuid) IS NULL OR v.access_point_id = CAST(:buildingId AS uuid))
        AND (CAST(:from AS timestamp) IS NULL OR v.arrival_time >= CAST(:from AS timestamp))
        AND (CAST(:to AS timestamp) IS NULL OR v.arrival_time < CAST(:to AS timestamp))
        AND (CAST(:search AS varchar) IS NULL OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
      countQuery =
          """
        SELECT COUNT(*) FROM visit v
        WHERE v.status = 'PRE_REGISTERED'
        AND (CAST(:status AS varchar) IS NULL OR v.status = CAST(:status AS varchar))
        AND (CAST(:buildingId AS uuid) IS NULL OR v.access_point_id = CAST(:buildingId AS uuid))
        AND (CAST(:from AS timestamp) IS NULL OR v.arrival_time >= CAST(:from AS timestamp))
        AND (CAST(:to AS timestamp) IS NULL OR v.arrival_time < CAST(:to AS timestamp))
        AND (CAST(:search AS varchar) IS NULL OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
      nativeQuery = true)
  Page<Visit> findPreRegistrations(
      @Param("status") String status,
      @Param("buildingId") UUID buildingId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("search") String search,
      Pageable pageable);
}
