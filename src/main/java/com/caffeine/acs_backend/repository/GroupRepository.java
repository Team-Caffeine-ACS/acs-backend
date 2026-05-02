package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Group;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, UUID> {

  Optional<Group> findByName(String name);
}
