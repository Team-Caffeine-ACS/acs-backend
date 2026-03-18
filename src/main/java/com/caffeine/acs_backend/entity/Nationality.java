package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nationality")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nationality extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    // ISO 3166-1 alpha-2, e.g. "EE", "US"
    @Column(name = "country_code", nullable = false, unique = true, length = 2)
    private String countryCode;
}
