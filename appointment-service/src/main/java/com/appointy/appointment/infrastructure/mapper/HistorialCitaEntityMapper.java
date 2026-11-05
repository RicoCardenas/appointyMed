package com.appointy.appointment.infrastructure.mapper;

import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.HistorialCitaEntity;
import org.springframework.stereotype.Component;

@Component
public class HistorialCitaEntityMapper {

    private final Conversiones c;

    public HistorialCitaEntityMapper(Conversiones c) {
        this.c = c;
    }

    public HistorialCita aDominio(HistorialCitaEntity e) {
        return new HistorialCita(c.idATexto(e.getId()), c.idATexto(e.getCitaId()), e.getEstadoAnterior(), e.getEstadoNuevo(),
                c.idATexto(e.getUsuarioId()), e.getRolUsuario(), c.idATexto(e.getFranjaAnteriorId()), c.idATexto(e.getFranjaNuevaId()),
                e.getDetalle(), c.instanteAMilisegundos(e.getFecha()));
    }

    public HistorialCitaEntity aEntidad(HistorialCita d) {
        HistorialCitaEntity e = new HistorialCitaEntity();
        e.setId(c.textoAId(d.id()));
        e.setCitaId(c.textoAId(d.citaId()));
        e.setEstadoAnterior(d.estadoAnterior());
        e.setEstadoNuevo(d.estadoNuevo());
        e.setUsuarioId(c.textoAId(d.usuarioId()));
        e.setRolUsuario(d.rolUsuario());
        e.setFranjaAnteriorId(c.textoAId(d.franjaAnteriorId()));
        e.setFranjaNuevaId(c.textoAId(d.franjaNuevaId()));
        e.setDetalle(d.detalle());
        e.setFecha(c.milisegundosAInstante(d.fecha()));
        return e;
    }
}
