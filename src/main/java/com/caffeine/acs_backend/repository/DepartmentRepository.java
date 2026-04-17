package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.Department;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

  List<Department> findAllByOrderByNameAsc();
}
