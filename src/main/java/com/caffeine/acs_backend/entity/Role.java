package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

  @Column(name = "name", nullable = false, length = 128)
  private String name;

  @Column(name = "description", length = 1024)
  private String description;
}
