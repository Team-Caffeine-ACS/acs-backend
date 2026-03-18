package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "keycard")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Keycard extends BaseEntity {

    @Column(name = "keycard_number", nullable = false, length = 128, unique = true)
    private String keycardNumber;

    // added isActive so a keycard can be deactivated (lost/stolen) without deletion
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // added validUntil for time-bounded keycard validity
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
}
