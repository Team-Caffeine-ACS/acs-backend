package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "keycard_in_possession")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycardInPossession extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "keycard_id", nullable = false)
  private Keycard keycard;

  @Column(name = "assigned_time", nullable = false)
  private LocalDateTime assignedTime;

  @Column(name = "return_time")
  private LocalDateTime returnTime;

  // changed from Person FK to PersonInRole FK — the role context matters
  // (e.g. a person could be a contractor one day and an employee another.
  // Shows in what role he holds card)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "keycard_holder_person_in_role_id", nullable = false)
  private PersonInRole keycardHolder;

  // (e.g. only a security officer role should be able to assign keycards)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "keycard_assignor_person_in_role_id", nullable = false)
  private PersonInRole keycardAssignor;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assigning_access_point_id", nullable = false)
  private AccessPoint assigningAccessPoint;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_access_point_id")
  private AccessPoint returnAccessPoint;
}
