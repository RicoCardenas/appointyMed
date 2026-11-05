package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.RegistrarPacientePort;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import com.appointy.auth.domain.rule.PoliticaClave;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class RegistrarPacienteUseCase implements RegistrarPacientePort {

    private final UsuarioRepositoryPort usuarios;
    private final PacientePerfilRepositoryPort perfiles;
    private final CodificadorClavePort codificador;
    private final Clock reloj;

    public RegistrarPacienteUseCase(
            UsuarioRepositoryPort usuarios,
            PacientePerfilRepositoryPort perfiles,
            CodificadorClavePort codificador,
            Clock reloj) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public PacienteConCuenta ejecutar(Comando comando) {
        String correo = NormalizadorCorreo.normalizar(comando.correo());
        PoliticaClave.validar(comando.clave());
        if (comando.fechaNacimiento() == null || comando.fechaNacimiento().isAfter(LocalDate.now(reloj))) {
            throw new ValidacionException("FECHA_NACIMIENTO_INVALIDA", "La fecha de nacimiento no puede ser futura");
        }
        String cedula = comando.cedula().trim();
        if (usuarios.existePorCorreo(correo)) {
            throw new ConflictoException("CORREO_YA_REGISTRADO", "El correo ya esta registrado");
        }
        if (perfiles.buscarPorCedula(cedula).isPresent()) {
            throw new ConflictoException("CEDULA_YA_REGISTRADA", "La cedula ya esta registrada");
        }
        Usuario usuario = usuarios.guardar(
                Usuario.nuevo(UUID.randomUUID(), correo, codificador.codificar(comando.clave()), Rol.PACIENTE, reloj.instant()));
        PacientePerfil perfil = perfiles.guardar(new PacientePerfil(
                usuario.id(),
                cedula,
                comando.nombres().trim(),
                comando.apellidos().trim(),
                comando.fechaNacimiento(),
                comando.telefono(),
                comando.direccion(),
                comando.ciudad()));
        return new PacienteConCuenta(usuario, perfil);
    }
}
