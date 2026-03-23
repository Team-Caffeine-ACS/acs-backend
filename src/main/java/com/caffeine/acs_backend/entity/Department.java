package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

  @Column(name = "name", nullable = false, length = 128)
  private String name;

  @Column(name = "description", length = 1024)
  private String description;
}
