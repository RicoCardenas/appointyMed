package com.appointy.appointment.infrastructure.mapper;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.CitaEntity;
import org.springframework.stereotype.Component;

@Component
public class CitaEntityMapper {

    private final Conversiones c;

    public CitaEntityMapper(Conversiones c) {
        this.c = c;
    }

    public Cita aDominio(CitaEntity e) {
        return new Cita(c.idATexto(e.getId()), c.idATexto(e.getPacienteId()), c.idATexto(e.getProfesionalId()),
                c.idATexto(e.getEspecialidadId()), c.idATexto(e.getCentroId()), c.idATexto(e.getFranjaId()), e.getEstado(),
                c.instanteAMilisegundos(e.getCreadoEn()), c.instanteAMilisegundos(e.getModificadoEn()), e.getVersion());
    }

    public CitaEntity aEntidad(Cita d) {
        CitaEntity e = new CitaEntity();
        e.setId(c.textoAId(d.id()));
        e.setPacienteId(c.textoAId(d.pacienteId()));
        e.setProfesionalId(c.textoAId(d.profesionalId()));
        e.setEspecialidadId(c.textoAId(d.especialidadId()));
        e.setCentroId(c.textoAId(d.centroId()));
        e.setFranjaId(c.textoAId(d.franjaId()));
        e.setEstado(d.estado());
        e.setCreadoEn(c.milisegundosAInstante(d.creadoEn()));
        e.setModificadoEn(c.milisegundosAInstante(d.modificadoEn()));
        e.setVersion(d.version());
        return e;
    }
}
