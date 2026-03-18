package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "access_zone")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessZone extends BaseEntity {

    // Groups AccessPoints into zones (floor, wing, building, etc.)
    // This allows authorization rules to be defined per-zone rather than per-door.

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;
}
