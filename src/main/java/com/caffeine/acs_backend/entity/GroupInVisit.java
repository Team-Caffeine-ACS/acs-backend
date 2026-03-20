package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "group_in_visit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInVisit extends BaseEntity {

  // the group-level visit event. Individual Visit records link back here via groupInVisit FK.

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "planned_arrival")
  private LocalDateTime plannedArrival;

  @Column(name = "planned_exit")
  private LocalDateTime plannedExit;

  @Column(name = "comment", length = 1024)
  private String comment;
}
