package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.CorregirDatosInmutablesPort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class CorregirDatosInmutablesUseCase implements CorregirDatosInmutablesPort {

    private static final Logger LOG = LoggerFactory.getLogger(CorregirDatosInmutablesUseCase.class);

    private final UsuarioRepositoryPort usuarios;
    private final PacientePerfilRepositoryPort perfiles;
    private final Clock reloj;

    public CorregirDatosInmutablesUseCase(UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles, Clock reloj) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public PacienteConCuenta ejecutar(Comando c) {
        if (c.cedula() == null && c.nombres() == null && c.apellidos() == null && c.fechaNacimiento() == null) {
            throw new ValidacionException("SIN_CAMBIOS", "Indica al menos un dato a corregir");
        }
        if (c.fechaNacimiento() != null && c.fechaNacimiento().isAfter(LocalDate.now(reloj))) {
            throw new ValidacionException("FECHA_NACIMIENTO_INVALIDA", "La fecha de nacimiento no puede ser futura");
        }
        Usuario usuario = usuarios.buscarPorId(c.pacienteUsuarioId())
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
        PacientePerfil perfil = perfiles.buscarPorUsuarioId(c.pacienteUsuarioId())
                .orElseThrow(() -> new NoEncontradoException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil de paciente"));

        String cedula = c.cedula() == null ? null : c.cedula().trim();
        if (cedula != null && !cedula.equals(perfil.cedula())
                && perfiles.buscarPorCedula(cedula).isPresent()) {
            throw new ConflictoException("CEDULA_YA_REGISTRADA", "La cedula ya esta registrada");
        }
        PacientePerfil corregido = perfiles.guardar(perfil.corregirIdentidad(
                cedula,
                c.nombres() == null ? null : c.nombres().trim(),
                c.apellidos() == null ? null : c.apellidos().trim(),
                c.fechaNacimiento()));
        // Se registra quien corrigio y que campos, nunca los valores (RNF-05).
        LOG.info("Identidad de paciente corregida por administrativo {} sobre usuario {}: cedula={}, nombres={}, apellidos={}, fechaNacimiento={}",
                c.actorId(), c.pacienteUsuarioId(), cedula != null, c.nombres() != null, c.apellidos() != null, c.fechaNacimiento() != null);
        return new PacienteConCuenta(usuario, corregido);
    }
}
