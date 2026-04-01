package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Person;
import com.caffeine.acs_backend.entity.PersonInRole;
import com.caffeine.acs_backend.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonInRoleRepository extends JpaRepository<PersonInRole, UUID> {

  @Query(
      "SELECT pir FROM PersonInRole pir"
          + " JOIN FETCH pir.person"
          + " JOIN FETCH pir.role"
          + " WHERE pir.isActive = true"
          + " AND (LOWER(pir.person.givenName) LIKE LOWER(CONCAT('%', :query, '%'))"
          + " OR LOWER(pir.person.surname) LIKE LOWER(CONCAT('%', :query, '%')))"
          + " AND (:role IS NULL OR LOWER(pir.role.name) = LOWER(:role))")
  List<PersonInRole> searchByPersonName(
      @Param("query") String query, @Param("role") String role);

  Optional<PersonInRole> findFirstByPersonAndIsActiveTrue(Person person);

  Optional<PersonInRole> findByPersonAndRoleAndIsActiveTrue(Person person, Role role);
}
