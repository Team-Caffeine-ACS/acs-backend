package com.caffeine.acs_backend.entity;

import com.caffeine.acs_backend.enums.VisitStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "visit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private VisitStatus status = VisitStatus.PRE_REGISTERED;

    // Planned arrival time set during pre-registration
    @Column(name = "planned_arrival_time")
    private LocalDateTime plannedArrivalTime;

    // Set when the visitor physically arrives
    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    // Set when the visitor leaves (story 9: end visit)
    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    // Nullable: access point may not be known at pre-registration time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_point_id")
    private AccessPoint accessPoint;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_person_in_role_id", nullable = false)
    private PersonInRole visitor;

    // Nullable: set only when this visit is part of a group visit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_in_visit_id")
    private GroupInVisit groupInVisit;

    // Nullable: not set for self-pre-registered visits; set by receptionist on arrival
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignor_person_in_role_id")
    private PersonInRole assignor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_person_in_role_id")
    private PersonInRole host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_person_in_role_id")
    private PersonInRole escort;

    @Column(name = "comment", length = 1024)
    private String comment;
}
