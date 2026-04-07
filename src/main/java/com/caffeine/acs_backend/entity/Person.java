package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "person")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person extends BaseEntity {

  @Column(name = "given_name", nullable = false, length = 128)
  private String givenName;

  @Column(name = "surname", nullable = false, length = 128)
  private String surname;

  @Column(name = "job_title", length = 128)
  private String jobTitle;

  @Column(name = "social_security_number", length = 128)
  private String socialSecurityNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id")
  private Organization organization;

  // added isActive to allow deactivating persons without deletion
  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private boolean isActive = true;

  // a person can have multiple documents
  @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Document> documents = new ArrayList<>();
}
