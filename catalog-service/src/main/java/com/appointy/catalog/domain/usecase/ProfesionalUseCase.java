package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.rule.Arreglos;
import com.appointy.catalog.domain.rule.ReglasConsulta;

/** crear profesional, actualizar profesional, desactivar profesional, consultar profesional, buscar profesional por usuario, listar profesionales, listar profesionales por especialidad, asociar especialidades, asociar centros, validar asociacion. */
public class ProfesionalUseCase {

    public record CrearProfesionalComando(String usuarioId, String nombres, String apellidos, String registroMedico) {}

    public record ActualizarProfesionalComando(String id, String nombres, String apellidos, String registroMedico) {}

    public record AsociarEspecialidadesComando(String profesionalId, String[] especialidadIds) {}

    public record AsociarCentrosComando(String profesionalId, String[] centroIds) {}

    public record ValidarAsociacionResultado(boolean profesionalActivo, boolean asociado) {}

    private final ProfesionalRepositoryGateway profesionales;
    private final EspecialidadRepositoryGateway especialidades;
    private final CentroRepositoryGateway centros;
    private final HorarioRepositoryGateway horarios;
    private final BloqueoRepositoryGateway bloqueos;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public ProfesionalUseCase(ProfesionalRepositoryGateway profesionales, EspecialidadRepositoryGateway especialidades, CentroRepositoryGateway centros, HorarioRepositoryGateway horarios, BloqueoRepositoryGateway bloqueos, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.profesionales = profesionales;
        this.especialidades = especialidades;
        this.centros = centros;
        this.horarios = horarios;
        this.bloqueos = bloqueos;
    }

    /** CU-11: crea el perfil de un profesional; usuarioId es la cuenta creada antes en auth. */
    public Profesional crearProfesional(CrearProfesionalComando c) {
        return transacciones.ejecutar(() -> {
            String registro = c.registroMedico().trim();
            if (profesionales.buscarPorUsuarioId(c.usuarioId()) != null) {
                throw new ConflictoException("PROFESIONAL_YA_EXISTE", "Esa cuenta ya tiene un perfil de profesional");
            }
            if (profesionales.buscarPorRegistroMedico(registro) != null) {
                throw new ConflictoException("REGISTRO_MEDICO_YA_REGISTRADO", "El registro medico ya esta registrado");
            }
            return profesionales.guardar(new Profesional(ids.nuevoId(), c.usuarioId(), c.nombres().trim(), c.apellidos().trim(), registro, true));
        });
    }

    /** CU-11: edita nombres, apellidos y registro medico (usuarioId no cambia). */
    public Profesional actualizarProfesional(ActualizarProfesionalComando c) {
        return transacciones.ejecutar(() -> {
            Profesional actual = profesionales.buscarPorId(c.id());
            if (actual == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            String registro = c.registroMedico().trim();
            Profesional conRegistro = profesionales.buscarPorRegistroMedico(registro);
            if (conRegistro != null && !conRegistro.id().equals(actual.id())) {
                throw new ConflictoException("REGISTRO_MEDICO_YA_REGISTRADO", "El registro medico ya esta registrado");
            }
            return profesionales.guardar(actual.actualizar(c.nombres().trim(), c.apellidos().trim(), registro));
        });
    }

    /** CU-11: borrado logico del profesional. */
    public void desactivarProfesional(String id) {
        transacciones.ejecutar(() -> {
            Profesional actual = profesionales.buscarPorId(id);
            if (actual == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            if (actual.activo()) {
                profesionales.guardar(actual.desactivar());
            }
    
            return null;
        });
    }

    /** Detalle de un profesional con sus especialidades y centros. */
    public DetalleProfesional consultarProfesional(String profesionalId) {
        return transacciones.ejecutarSoloLectura(() -> {
            Profesional p = profesionales.buscarPorId(profesionalId);
            if (p == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            return new DetalleProfesional(p, profesionales.idsEspecialidades(profesionalId), profesionales.idsCentros(profesionalId));
        });
    }

    /** Consulta interna de appointment (CU-22, CU-23): el profesional que corresponde a una cuenta de auth. */
    public Profesional buscarProfesionalPorUsuario(String usuarioId) {
        return transacciones.ejecutarSoloLectura(() -> {
            return profesionales.buscarPorUsuarioId(usuarioId);
        });
    }

    /** Listado paginado de profesionales (RA-04), ordenado por apellidos y nombres. */
    public PaginaProfesionales listarProfesionales(FiltroProfesionales filtro, int pagina, int tamano) {
        return transacciones.ejecutarSoloLectura(() -> {
            ReglasConsulta.validarPaginacion(pagina, tamano);
            return profesionales.listar(filtro, pagina, tamano);
        });
    }

    /** CU-09: profesionales activos de una especialidad activa. */
    public Profesional[] listarProfesionalesPorEspecialidad(String especialidadId) {
        return transacciones.ejecutarSoloLectura(() -> {
            Especialidad especialidad = especialidades.buscarPorId(especialidadId);
            if (especialidad == null || !especialidad.activo()) {
                throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad no existe o no esta disponible");
            }
            return profesionales.listarActivosPorEspecialidad(especialidadId);
        });
    }

    /** CU-12: reemplaza las especialidades del profesional por las indicadas (todas deben existir y estar activas). */
    public DetalleProfesional asociarEspecialidades(AsociarEspecialidadesComando c) {
        return transacciones.ejecutar(() -> {
            Profesional p = profesionales.buscarPorId(c.profesionalId());
            if (p == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            String[] nuevas = Arreglos.sinDuplicados(c.especialidadIds());
            for (String id : nuevas) {
                Especialidad e = especialidades.buscarPorId(id);
                if (e == null) {
                    throw new NoEncontradoException("ESPECIALIDAD_NO_ENCONTRADA", "La especialidad " + id + " no existe");
                }
                if (!e.activo()) {
                    throw new ValidacionException("ESPECIALIDAD_INACTIVA", "La especialidad " + id + " esta inactiva");
                }
            }
            profesionales.reemplazarEspecialidades(p.id(), nuevas);
            return new DetalleProfesional(p, nuevas, profesionales.idsCentros(p.id()));
        });
    }

    /** CU-12: reemplaza los centros del profesional; no se puede quitar un centro que tenga horarios o bloqueos. */
    public DetalleProfesional asociarCentros(AsociarCentrosComando c) {
        return transacciones.ejecutar(() -> {
            Profesional p = profesionales.buscarPorId(c.profesionalId());
            if (p == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            String[] nuevos = Arreglos.sinDuplicados(c.centroIds());
            for (String id : nuevos) {
                Centro centro = centros.buscarPorId(id);
                if (centro == null) {
                    throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro " + id + " no existe");
                }
                if (!centro.activo()) {
                    throw new ValidacionException("CENTRO_INACTIVO", "El centro " + id + " esta inactivo");
                }
            }
            for (String actual : profesionales.idsCentros(p.id())) {
                if (!Arreglos.contiene(nuevos, actual)
                        && (horarios.contarPorProfesionalYCentro(p.id(), actual) > 0 || bloqueos.contarPorProfesionalYCentro(p.id(), actual) > 0)) {
                    throw new ConflictoException("CENTRO_CON_HORARIOS", "No se puede quitar el centro " + actual + ": tiene horarios o bloqueos del profesional");
                }
            }
            profesionales.reemplazarCentros(p.id(), nuevos);
            return new DetalleProfesional(p, profesionales.idsEspecialidades(p.id()), nuevos);
        });
    }

    /** Consulta interna de appointment (CU-18): profesional activo y asociado a la especialidad y al centro. */
    public ValidarAsociacionResultado validarAsociacion(String profesionalId, String especialidadId, String centroId) {
        return transacciones.ejecutarSoloLectura(() -> {
            Profesional p = profesionales.buscarPorId(profesionalId);
            if (p == null) {
                return new ValidarAsociacionResultado(false, false);
            }
            boolean asociado = profesionales.tieneEspecialidad(profesionalId, especialidadId) && profesionales.atiendeEnCentro(profesionalId, centroId);
            return new ValidarAsociacionResultado(p.activo(), asociado);
        });
    }
}
