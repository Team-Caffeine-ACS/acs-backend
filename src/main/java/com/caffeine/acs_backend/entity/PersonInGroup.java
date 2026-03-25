package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "person_in_group")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonInGroup extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_in_role_id", nullable = false)
  private PersonInRole personInRole;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;
}
