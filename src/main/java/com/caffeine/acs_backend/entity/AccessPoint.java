package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "access_point")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPoint extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "address", length = 128)
    private String address;

    // access points now belong to a zone for better hierarchical authorization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_zone_id")
    private AccessZone zone;
}
