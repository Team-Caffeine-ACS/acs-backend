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
              + "  v.id                                                     AS \"id\","
              + "  p.given_name || ' ' || p.surname                         AS \"fullName\","
              + "  (SELECT d.document_number FROM document d"
              + "   WHERE d.person_id = p.id LIMIT 1)                       AS \"documentNumber\","
              + "  CASE WHEN hp.id IS NOT NULL"
              + "    THEN hp.given_name || ' ' || hp.surname"
              + "    ELSE NULL END                                           AS \"hostName\","
              + "  v.arrival_time                                            AS \"entryTime\","
              + "  v.exit_time                                               AS \"exitTime\","
              + "  CASE"
              + "    WHEN v.arrival_time > CURRENT_TIMESTAMP                THEN 'planned'"
              + "    WHEN v.exit_time IS NOT NULL"
              + "         AND v.exit_time <= CURRENT_TIMESTAMP               THEN 'departed'"
              + "    WHEN v.arrival_time < CURRENT_DATE                     THEN 'expired'"
              + "    ELSE                                                        'in_building'"
              + "  END                                                       AS \"status\","
              + "  p.id                                                      AS \"visitorId\""
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
              + "     OR (:status = 'planned'     AND v.arrival_time > CURRENT_TIMESTAMP)"
              + "     OR (:status = 'in_building' AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time <= CURRENT_TIMESTAMP"
              + "                                 AND v.arrival_time >= CURRENT_DATE)"
              + "     OR (:status = 'departed'    AND v.exit_time IS NOT NULL"
              + "                                 AND v.exit_time <= CURRENT_TIMESTAMP)"
              + "     OR (:status = 'expired'     AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time < CURRENT_DATE)"
              + "   )"
              + "   AND (CAST(:dateFrom AS timestamp) IS NULL"
              + "        OR v.arrival_time >= CAST(:dateFrom AS timestamp))"
              + "   AND (CAST(:dateTo AS timestamp) IS NULL"
              + "        OR v.arrival_time <= CAST(:dateTo AS timestamp))"
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
              + "     OR (:status = 'planned'     AND v.arrival_time > CURRENT_TIMESTAMP)"
              + "     OR (:status = 'in_building' AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time <= CURRENT_TIMESTAMP"
              + "                                 AND v.arrival_time >= CURRENT_DATE)"
              + "     OR (:status = 'departed'    AND v.exit_time IS NOT NULL"
              + "                                 AND v.exit_time <= CURRENT_TIMESTAMP)"
              + "     OR (:status = 'expired'     AND v.exit_time IS NULL"
              + "                                 AND v.arrival_time < CURRENT_DATE)"
              + "   )"
              + "   AND (CAST(:dateFrom AS timestamp) IS NULL"
              + "        OR v.arrival_time >= CAST(:dateFrom AS timestamp))"
              + "   AND (CAST(:dateTo AS timestamp) IS NULL"
              + "        OR v.arrival_time <= CAST(:dateTo AS timestamp))")
  Page<VisitListView> findAllFiltered(
      @Param("search") String search,
      @Param("status") String status,
      @Param("dateFrom") LocalDateTime dateFrom,
      @Param("dateTo") LocalDateTime dateTo,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT v.id, v.access_point_id, v.arrival_time,
              v.assignor_person_in_role_id, v.comment, v.escort_person_in_role_id,
              v.exit_time, v.group_in_visit_id, v.host_person_in_role_id,
              v.notes, v.status, v.visitor_person_in_role_id, v.created_at
          FROM visit v
          WHERE (CAST(:status AS varchar) IS NULL OR v.status = CAST(:status AS varchar))
          AND (CAST(:buildingId AS uuid) IS NULL OR v.access_point_id = CAST(:buildingId AS uuid))
          AND (CAST(:from AS timestamp) IS NULL OR v.arrival_time >= CAST(:from AS timestamp))
          AND (CAST(:to AS timestamp) IS NULL OR v.arrival_time < CAST(:to AS timestamp))
          AND (CAST(:search AS varchar) IS NULL OR LOWER(v.notes) LIKE LOWER(CONCAT('%', :search, '%')))
          """,
      countQuery =
          """
          SELECT COUNT(*) FROM visit v
          WHERE (CAST(:status AS varchar) IS NULL OR v.status = CAST(:status AS varchar))
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

  // 1. Loendab hetkel majas viibijaid (aktiivsed külastajad)
  @Query(
      "SELECT COUNT(v) FROM Visit v "
          + "WHERE v.exitTime IS NULL "
          + "AND v.arrivalTime <= CURRENT_TIMESTAMP "
          + "AND (:buildingId IS NULL OR v.accessPoint.id = :buildingId)")
  long countCurrentActiveVisitors(@Param("buildingId") UUID buildingId);

  // 2. Loendab tänaseks planeeritud külastusi (broneeringud)
  @Query(
      "SELECT COUNT(v) FROM Visit v "
          + "WHERE v.arrivalTime >= :from "
          + "AND v.arrivalTime < :to "
          + "AND (:buildingId IS NULL OR v.accessPoint.id = :buildingId)")
  long countBookingsByDate(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("buildingId") UUID buildingId);

  // 3. Toob nimekirja viimastest külastajatest (Recent Visits)
  // Kasutame siin sarnast loogikat nagu findAllFiltered, aga lihtsamalt
  @Query(
      "SELECT v FROM Visit v "
          + "WHERE v.arrivalTime >= :since "
          + "AND (:buildingId IS NULL OR v.accessPoint.id = :buildingId) "
          + "ORDER BY v.arrivalTime DESC")
  List<Visit> findRecentVisits(
      @Param("since") LocalDateTime since, @Param("buildingId") UUID buildingId, Pageable pageable);

    long countByStatus(VisitStatus status);

    // Loendame tänased broneeringud (kõik, mis pole tühistatud ja on tänase kuupäevaga)
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :startOfDay AND v.status != 'CANCELLED'")
    long countTodayBookings(@Param("startOfDay") LocalDateTime startOfDay);

    // Loendame aktiivsed külastajad konkreetses hoones
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.status = :status AND v.accessPoint.building.id = :buildingId")
    long countByStatusAndBuilding(@Param("status") VisitStatus status, @Param("buildingId") UUID buildingId);

    // Loendame tänased broneeringud konkreetses hoones
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :since " +
           "AND v.status != 'CANCELLED' " +
           "AND v.accessPoint.building.id = :buildingId")
    long countTodayBookingsByBuilding(@Param("since") LocalDateTime since, @Param("buildingId") UUID buildingId);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.arrivalTime >= :start AND v.arrivalTime < :end " +
       "AND v.accessPoint.building.id = :buildingId AND v.status != 'CANCELLED'")
    long countVisitsInPeriod(@Param("start") LocalDateTime start, 
                         @Param("end") LocalDateTime end, 
                         @Param("buildingId") UUID buildingId);
}
