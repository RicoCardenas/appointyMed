package com.appointy.clinicalhistory.infrastructure.mapper;

import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.EntradaClinicaEntity;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.LogAccesoEntity;
import org.springframework.stereotype.Component;

@Component
public class ClinicaEntityMapper {

    private final Conversiones c;

    public ClinicaEntityMapper(Conversiones c) {
        this.c = c;
    }

    public EntradaClinica aDominio(EntradaClinicaEntity e) {
        return new EntradaClinica(c.idATexto(e.getId()), c.idATexto(e.getPacienteId()), c.idATexto(e.getProfesionalId()), c.idATexto(e.getCitaId()),
                e.getMotivo(), e.getDiagnostico(), e.getNotas(), e.getIndicaciones(), c.idATexto(e.getCorrigeAId()), c.instanteAMilisegundos(e.getCreadoEn()));
    }

    public EntradaClinicaEntity aEntidad(EntradaClinica d) {
        EntradaClinicaEntity e = new EntradaClinicaEntity();
        e.setId(c.textoAId(d.id()));
        e.setPacienteId(c.textoAId(d.pacienteId()));
        e.setProfesionalId(c.textoAId(d.profesionalId()));
        e.setCitaId(c.textoAId(d.citaId()));
        e.setMotivo(d.motivo());
        e.setDiagnostico(d.diagnostico());
        e.setNotas(d.notas());
        e.setIndicaciones(d.indicaciones());
        e.setCorrigeAId(c.textoAId(d.corrigeAId()));
        e.setCreadoEn(c.milisegundosAInstante(d.creadoEn()));
        return e;
    }

    public LogAccesoEntity aEntidad(LogAcceso d) {
        LogAccesoEntity e = new LogAccesoEntity();
        e.setId(c.textoAId(d.id()));
        e.setUsuarioId(c.textoAId(d.usuarioId()));
        e.setRol(d.rol());
        e.setPacienteId(c.textoAId(d.pacienteId()));
        e.setAccion(d.accion());
        e.setEntradaId(c.textoAId(d.entradaId()));
        e.setFecha(c.milisegundosAInstante(d.fecha()));
        e.setResultado(d.resultado());
        return e;
    }
}
