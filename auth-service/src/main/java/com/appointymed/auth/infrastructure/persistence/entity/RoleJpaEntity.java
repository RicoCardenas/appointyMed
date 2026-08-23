package com.appointymed.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleJpaEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

}