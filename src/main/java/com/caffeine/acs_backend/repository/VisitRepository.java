package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Visit;
import com.caffeine.acs_backend.enums.VisitStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitRepository extends JpaRepository<Visit, UUID> {

  @Query(
      nativeQuery = true,
      value =
          "SELECT"
              + "  v.id                                                      AS \"id\","
              + "  p.given_name || ' ' || p.surname                          AS \"fullName\","
              + "  (SELECT d.document_number FROM document d"
              + "   WHERE d.person_id = p.id LIMIT 1)                        AS \"documentNumber\","
              + "  org.name                                                    AS \"organizationName\","
              + "  CASE WHEN hp.id IS NOT NULL"
              + "   THEN hp.given_name || ' ' || hp.surname"
              + "   ELSE NULL END                                            AS \"hostName\","
              + "  v.arrival_time                                            AS \"entryTime\","
              + "  v.exit_time                                               AS \"exitTime\","
              + "  CASE"
              + "    /* 1. Tulevik: Kui saabumisaeg on alles ees */ "
              + "    WHEN v.arrival_time > CURRENT_TIMESTAMP THEN 'PLANNED'"
              + "    /* 2. Lahkunud: Kui lahkumisaeg on täidetud ja see on möödas */ "
              + "    WHEN v.exit_time IS NOT NULL AND v.exit_time <= CURRENT_TIMESTAMP THEN 'DEPARTED'"
              + "    /* 3. Aegunud: Kui lahkumist pole märgitud, aga saabumiskuupäev oli ENNE tänast */ "
              + "    WHEN v.exit_time IS NULL AND v.arrival_time < CURRENT_DATE THEN 'EXPIRED'"
              + "    /* 4. Kõik muu: Saabumisaeg on käes/möödas/täna ja lahkumist pole (st on praegu hoones) */ "
              + "    ELSE 'IN_BUILDING'"
              + "  END                                                       AS \"visitStatus\","
              + "  p.id                                                      AS \"visitorId\","
              + "  v.access_point_id                                         AS \"accessPointId\","
              + "  ap.name                                                   AS \"accessPointName\","
              + "  ap.address                                                AS \"accessPointAddress\""
              + " FROM visit v"
              + " JOIN access_point ap ON v.access_point_id = ap.id"
              + " JOIN person_in_role pir  ON pir.id = v.visitor_person_in_role_id"
              + " JOIN person p            ON p.id = pir.person_id"
              + " LEFT JOIN person_in_role hpir ON hpir.id = v.host_person_in_role_id"
              + " LEFT JOIN person hp      ON hp.id = hpir.person_id"
              + " JOIN organization org ON org.id = p.organization_id"
              + " WHERE"
              + "   ( CAST(:search AS text) IS NULL"
              + "     OR (p.given_name || ' ' || p.surname) ILIKE '%' || :search || '%'"
              + "     OR EXISTS (SELECT 1 FROM document d"
              + "                WHERE d.person_id = p.id"
              + "                AND d.document_number ILIKE '%' || :search || '%')"
              + "     OR (hp.given_name || ' ' || hp.surname) ILIKE '%' || :search || '%'"
              + "   )"
              + "   AND"
              + "   ( CAST(:status AS text) IS NULL"
              + "     OR (:status = 'PLANNED'     AND v.arrival_time > CURRENT_TIMESTAMP)"
              + "     OR (:status = 'IN_BUILDING' AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time <= CURRENT_TIMESTAMP"
              + "                                 AND v.arrival_time >= CURRENT_DATE)"
              + "     OR (:status = 'DEPARTED'    AND v.exit_time IS NOT NULL"
              + "                                 AND v.exit_time <= CURRENT_TIMESTAMP)"
              + "     OR (:status = 'EXPIRED'     AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time < CURRENT_DATE)"
              + "   )"
              + "   AND (CAST(:dateFrom AS timestamp) IS NULL"
              + "        OR v.arrival_time >= CAST(:dateFrom AS timestamp))"
              + "   AND (CAST(:dateTo AS timestamp) IS NULL"
              + "        OR v.arrival_time <= CAST(:dateTo AS timestamp))"
              + "   AND (CAST(:accessPointId AS uuid) IS NULL"
              + "        OR v.access_point_id = CAST(:accessPointId AS uuid))"
              + " ORDER BY v.arrival_time DESC",
      countQuery =
          "SELECT COUNT(*)"
              + " FROM visit v"
              + " JOIN person_in_role pir  ON pir.id = v.visitor_person_in_role_id"
              + " JOIN person p            ON p.id = pir.person_id"
              + " LEFT JOIN person_in_role hpir ON hpir.id = v.host_person_in_role_id"
              + " LEFT JOIN person hp      ON hp.id = hpir.person_id"
              + " WHERE"
              + "   ( CAST(:search AS text) IS NULL"
              + "     OR (p.given_name || ' ' || p.surname) ILIKE '%' || :search || '%'"
              + "     OR EXISTS (SELECT 1 FROM document d"
              + "                WHERE d.person_id = p.id"
              + "                AND d.document_number ILIKE '%' || :search || '%')"
              + "     OR (hp.given_name || ' ' || hp.surname) ILIKE '%' || :search || '%'"
              + "   )"
              + "   AND"
              + "   ( CAST(:status AS text) IS NULL"
              + "     OR (:status = 'PLANNED'     AND v.arrival_time > CURRENT_TIMESTAMP)"
              + "     OR (:status = 'IN_BUILDING' AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time <= CURRENT_TIMESTAMP"
              + "                                 AND v.arrival_time >= CURRENT_DATE)"
              + "     OR (:status = 'DEPARTED'    AND v.exit_time IS NOT NULL"
              + "                                 AND v.exit_time <= CURRENT_TIMESTAMP)"
              + "     OR (:status = 'EXPIRED'     AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time < CURRENT_DATE)"
              + "   )"
              + "   AND (CAST(:dateFrom AS timestamp) IS NULL"
              + "        OR v.arrival_time >= CAST(:dateFrom AS timestamp))"
              + "   AND (CAST(:dateTo AS timestamp) IS NULL"
              + "        OR v.arrival_time <= CAST(:dateTo AS timestamp))"
              + "   AND (CAST(:accessPointId AS uuid) IS NULL"
              + "        OR v.access_point_id = CAST(:accessPointId AS uuid))")
  Page<VisitListView> findAllFiltered(
      @Param("search") String search,
      @Param("status") String status,
      @Param("dateFrom") LocalDateTime dateFrom,
      @Param("dateTo") LocalDateTime dateTo,
      @Param("accessPointId") UUID accessPointId,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT v.id, v.access_point_id, v.arrival_time,
              v.assignor_person_in_role_id, v.comment, v.escort_person_in_role_id,
              v.exit_time, v.group_in_visit_id, v.host_person_in_role_id,
              v.notes, v.status, v.visitor_person_in_role_id, v.created_at
          FROM visit v
          WHERE (
            CAST(:status AS varchar) IS NULL
            OR (:status = 'PLANNED' AND v.status IN ('PLANNED', 'PRE_REGISTERED'))
            OR v.status = CAST(:status AS varchar)
          )
          AND (CAST(:buildingId AS uuid) IS NULL OR v.access_point_id = CAST(:buildingId AS uuid))
          AND (CAST(:from AS timestamp) IS NULL OR v.arrival_time >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR v.arrival_time < CAST(:to AS timestamp))
          AND (CAST(:search AS varchar) IS NULL OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%')))
          """,
      countQuery =
          """
          SELECT COUNT(*) FROM visit v
          WHERE (
            CAST(:status AS varchar) IS NULL
            OR (:status = 'PLANNED' AND v.status IN ('PLANNED', 'PRE_REGISTERED'))
            OR v.status = CAST(:status AS varchar)
          )
          AND (CAST(:buildingId AS uuid) IS NULL OR v.access_point_id = CAST(:buildingId AS uuid))
          AND (CAST(:from AS timestamp) IS NULL OR v.arrival_time >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR v.arrival_time < CAST(:to AS timestamp))
          AND (CAST(:search AS varchar) IS NULL OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%')))
          """,
      nativeQuery = true)
  Page<Visit> findPreRegistrations(
      @Param("status") String status,
      @Param("accessPointId") UUID accessPointId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("search") String search,
      Pageable pageable);

  long countByStatusAndAccessPointId(VisitStatus status, UUID accessPointId);

  @Query(
      "SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :since "
          + "AND v.status != com.caffeine.acs_backend.enums.VisitStatus.EXPIRED "
          + "AND v.accessPoint.id = :accessPointId")
  long countTodayVisitsByAccessPointId(
      @Param("since") LocalDateTime since, @Param("accessPointId") UUID accessPointId);

  @Query(
      "SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :start AND v.arrivalTime < :end "
          + "AND v.accessPoint.id = :accessPointId")
  long countVisitsInPeriodByAccessPointId(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("accessPointId") UUID accessPointId);

  // Toob nimekirja viimastest külastajatest (Recent Visits)
  @Query(
      "SELECT v FROM Visit v "
          + "WHERE v.arrivalTime >= :since "
          + "AND (:accessPointId IS NULL OR v.accessPoint.id = :accessPointId) "
          + "ORDER BY v.arrivalTime DESC")
  List<Visit> findRecentVisits(
      @Param("since") LocalDateTime since,
      @Param("accessPointId") UUID accessPointId,
      Pageable pageable);

  long countByStatus(VisitStatus status);

  // Loendame tänased broneeringud (kõik, mis pole tühistatud ja on tänase kuupäevaga)
  @Query(
      "SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :startOfDay AND v.status != 'EXPIRED'")
  long countTodayBookings(@Param("startOfDay") LocalDateTime startOfDay);

  @Query(
      "SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :start AND v.arrivalTime < :end "
          + "AND v.status != 'EXPIRED'")
  long countVisitsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  /**
   * Find all individual visits belonging to a group visit. Used by VisitGroupService to list
   * members and compute group-level statistics.
   */
  List<Visit> findByGroupInVisitId(UUID groupInVisitId);
}
