package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Country;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, UUID> {

  List<Country> findAllByOrderByNameAsc();
}
