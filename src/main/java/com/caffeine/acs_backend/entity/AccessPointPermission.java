package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "access_point_permission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPointPermission extends BaseEntity {

    // Defines which roles are authorized to access which access points,
    // with optional time-based restrictions. Without this, the system can log visits but cannot
    // determine whether access should be granted.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "access_point_id", nullable = false)
    private AccessPoint accessPoint;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    // Bitmask: bit 0=Mon, bit 1=Tue, bit 2=Wed, bit 3=Thu, bit 4=Fri, bit 5=Sat, bit 6=Sun
    // 0x7F (127) = all days, 0x1F (31) = weekdays only
    @Column(name = "allowed_days_mask", nullable = false)
    @Builder.Default
    private int allowedDaysMask = 0x7F;

    @Column(name = "time_from")
    private LocalTime timeFrom;

    @Column(name = "time_to")
    private LocalTime timeTo;
}
