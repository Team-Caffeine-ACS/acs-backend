package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.AccessPoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessPointRepository extends JpaRepository<AccessPoint, UUID> {

  List<AccessPoint> findAllByOrderByNameAsc();
}
