package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Organization;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  List<Organization> findAllByOrderByNameAsc();
}
