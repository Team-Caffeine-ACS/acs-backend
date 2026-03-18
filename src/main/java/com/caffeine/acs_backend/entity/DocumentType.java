package com.caffeine.acs_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;
}
