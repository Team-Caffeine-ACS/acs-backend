package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Visit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, UUID> {}
