package com.caffeine.acs_backend.repository;

import com.caffeine.acs_backend.entity.DocumentType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {}
