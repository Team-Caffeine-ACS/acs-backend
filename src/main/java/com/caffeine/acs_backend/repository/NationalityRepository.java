package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Nationality;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NationalityRepository extends JpaRepository<Nationality, UUID> {

  List<Nationality> findAllByOrderByNameAsc();
}
