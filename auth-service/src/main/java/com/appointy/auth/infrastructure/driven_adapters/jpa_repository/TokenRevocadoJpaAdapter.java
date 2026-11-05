package com.appointy.auth.infrastructure.driven_adapters.jpa_repository;

import com.appointy.auth.domain.model.TokenRevocado;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.TokenRevocadoEntity;
import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository.SpringDataTokenRevocadoRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TokenRevocadoJpaAdapter implements TokenRevocadoRepositoryPort {

    private final SpringDataTokenRevocadoRepository repositorio;

    public TokenRevocadoJpaAdapter(SpringDataTokenRevocadoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void guardar(TokenRevocado token) {
        TokenRevocadoEntity entidad = new TokenRevocadoEntity();
        entidad.setJti(token.jti());
        entidad.setExpiraEn(token.expiraEn());
        repositorio.save(entidad);
    }

    @Override
    public boolean existePorJti(String jti) {
        return repositorio.existsById(jti);
    }

    @Override
    public int eliminarVencidosAntesDe(Instant limite) {
        return repositorio.eliminarVencidosAntesDe(limite);
    }
}
