package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "person_in_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonInRole extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  private Person person;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "comment", length = 1024)
  private String comment;

  // added isActive so a role assignment can be revoked without deletion
  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private boolean isActive = true;

  // added validUntil for time-bounded role assignments
  @Column(name = "valid_until")
  private LocalDateTime validUntil;
}
