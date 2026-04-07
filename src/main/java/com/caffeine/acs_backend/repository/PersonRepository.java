package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Person;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, UUID> {}
