package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.GroupInVisit;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupInVisitRepository extends JpaRepository<GroupInVisit, UUID> {

  @Query(
      nativeQuery = true,
      value =
          "SELECT giv.*"
              + " FROM group_in_visit giv"
              + " JOIN visitor_group g ON g.id = giv.group_id"
              + " WHERE"
              + "   (CAST(:search AS text) IS NULL"
              + "     OR g.name ILIKE '%' || :search || '%'"
              + "     OR giv.comment ILIKE '%' || :search || '%')"
              + "   AND (CAST(:from AS timestamp) IS NULL"
              + "     OR giv.planned_arrival >= CAST(:from AS timestamp))"
              + "   AND (CAST(:to AS timestamp) IS NULL"
              + "     OR giv.planned_arrival < CAST(:to AS timestamp))"
              + " ORDER BY giv.planned_arrival DESC",
      countQuery =
          "SELECT COUNT(*)"
              + " FROM group_in_visit giv"
              + " JOIN visitor_group g ON g.id = giv.group_id"
              + " WHERE"
              + "   (CAST(:search AS text) IS NULL"
              + "     OR g.name ILIKE '%' || :search || '%'"
              + "     OR giv.comment ILIKE '%' || :search || '%')"
              + "   AND (CAST(:from AS timestamp) IS NULL"
              + "     OR giv.planned_arrival >= CAST(:from AS timestamp))"
              + "   AND (CAST(:to AS timestamp) IS NULL"
              + "     OR giv.planned_arrival < CAST(:to AS timestamp))")
  Page<GroupInVisit> findAllFiltered(
      @Param("search") String search,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);
}