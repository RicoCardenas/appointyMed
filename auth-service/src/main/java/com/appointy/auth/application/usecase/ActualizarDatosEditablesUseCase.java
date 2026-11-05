package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.ActualizarDatosEditablesPort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;

public class ActualizarDatosEditablesUseCase implements ActualizarDatosEditablesPort {

    private final UsuarioRepositoryPort usuarios;
    private final PacientePerfilRepositoryPort perfiles;
    private final Clock reloj;

    public ActualizarDatosEditablesUseCase(UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles, Clock reloj) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public PacienteConCuenta ejecutar(Comando c) {
        if (c.correo() == null && c.telefono() == null && c.direccion() == null && c.ciudad() == null) {
            throw new ValidacionException("SIN_CAMBIOS", "Indica al menos un dato a actualizar");
        }
        Usuario usuario = usuarios.buscarPorId(c.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
        PacientePerfil perfil = perfiles.buscarPorUsuarioId(c.usuarioId())
                .orElseThrow(() -> new NoEncontradoException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil de paciente"));

        if (c.correo() != null) {
            String nuevoCorreo = NormalizadorCorreo.normalizar(c.correo());
            if (!nuevoCorreo.equals(usuario.correo())) {
                if (usuarios.existePorCorreo(nuevoCorreo)) {
                    throw new ConflictoException("CORREO_YA_REGISTRADO", "El correo ya esta registrado");
                }
                usuario = usuarios.guardar(usuario.conCorreo(nuevoCorreo, reloj.instant()));
            }
        }
        perfil = perfiles.guardar(perfil.actualizarContacto(c.telefono(), c.direccion(), c.ciudad()));
        return new PacienteConCuenta(usuario, perfil);
    }
}
