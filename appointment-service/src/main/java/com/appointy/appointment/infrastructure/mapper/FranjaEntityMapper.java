package com.appointy.appointment.infrastructure.mapper;

import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.FranjaEntity;
import org.springframework.stereotype.Component;

@Component
public class FranjaEntityMapper {

    private final Conversiones c;

    public FranjaEntityMapper(Conversiones c) {
        this.c = c;
    }

    public Franja aDominio(FranjaEntity e) {
        return new Franja(c.idATexto(e.getId()), c.idATexto(e.getProfesionalId()), c.idATexto(e.getCentroId()), c.fechaATexto(e.getFecha()),
                c.horaAMinutos(e.getHoraInicio()), c.horaAMinutos(e.getHoraFin()), e.getEstado(), e.getVersion());
    }

    public FranjaEntity aEntidad(Franja d) {
        FranjaEntity e = new FranjaEntity();
        e.setId(c.textoAId(d.id()));
        e.setProfesionalId(c.textoAId(d.profesionalId()));
        e.setCentroId(c.textoAId(d.centroId()));
        e.setFecha(c.textoAFecha(d.fecha()));
        e.setHoraInicio(c.minutosAHora(d.horaInicio()));
        e.setHoraFin(c.minutosAHora(d.horaFin()));
        e.setEstado(d.estado());
        e.setVersion(d.version());
        return e;
    }
}
