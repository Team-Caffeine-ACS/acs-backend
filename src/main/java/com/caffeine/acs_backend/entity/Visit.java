package com.caffeine.acs_backend.entity;

import com.caffeine.acs_backend.enums.VisitAccessLevel;
import com.caffeine.acs_backend.enums.VisitStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "visit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit extends BaseEntity {

  @Column(name = "arrival_time", nullable = false)
  private LocalDateTime arrivalTime;

  @Column(name = "exit_time")
  private LocalDateTime exitTime;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "access_point_id", nullable = false)
  private AccessPoint accessPoint;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "visitor_person_in_role_id", nullable = false)
  private PersonInRole visitor;

  // Nullable: set only when this visit is part of a group visit
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_in_visit_id")
  private GroupInVisit groupInVisit;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assignor_person_in_role_id", nullable = false)
  private PersonInRole assignor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host_person_in_role_id")
  private PersonInRole host;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "escort_person_in_role_id")
  private PersonInRole escort;

  @Column(name = "comment", length = 1024)
  private String comment;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private VisitStatus status = VisitStatus.PRE_REGISTERED;

  @Enumerated(EnumType.STRING)
  @Column(name = "access_level", length = 32)
  private VisitAccessLevel VisitAccessLevel;

  @Column(name = "notes", length = 1024)
  private String notes;

  @Column(name = "visitor_email", length = 255)
  private String visitorEmail;

  @Column(name = "visitor_full_name", length = 255)
  private String visitorFullName;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
