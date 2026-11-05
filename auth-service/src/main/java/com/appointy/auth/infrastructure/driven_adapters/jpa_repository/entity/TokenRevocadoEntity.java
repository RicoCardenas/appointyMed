package com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "token_revocado")
@Getter
@Setter
@NoArgsConstructor
public class TokenRevocadoEntity {

    @Id
    @Column(name = "jti")
    private String jti;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
}
