package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.RegistroGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import com.appointy.auth.domain.gateway.TransactionGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.NombrePaciente;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import com.appointy.auth.domain.rule.ReglasUsuarios;

/** Perfil del paciente: consulta (CU-04), edicion (CU-05), correccion administrativa (CU-07) y consultas internas. */
public class PerfilUseCase {

    /** Un valor nulo significa "sin cambio". */
    public record ActualizarDatosEditablesComando(String usuarioId, String correo, String telefono, String direccion, String ciudad) {}

    /** Un valor nulo significa "sin cambio". {@code fechaNacimiento}: texto ISO. */
    public record CorregirDatosInmutablesComando(String actorId, String pacienteUsuarioId, String cedula, String nombres,
            String apellidos, String fechaNacimiento) {}

    private final UsuarioRepositoryGateway usuarios;
    private final PacientePerfilRepositoryGateway perfiles;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final RegistroGateway registro;

    public PerfilUseCase(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, RelojGateway reloj,
            TransactionGateway transacciones, RegistroGateway registro) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.reloj = reloj;
        this.transacciones = transacciones;
        this.registro = registro;
    }

    public PacienteConCuenta consultarPerfil(String usuarioId) {
        return transacciones.ejecutarSoloLectura(
                () -> new PacienteConCuenta(Busquedas.usuario(usuarios, usuarioId), Busquedas.perfil(perfiles, usuarioId)));
    }

    /** CU-05: correo, telefono, direccion y ciudad. */
    public PacienteConCuenta actualizarDatosEditables(ActualizarDatosEditablesComando c) {
        return transacciones.ejecutar(() -> {
            if (c.correo() == null && c.telefono() == null && c.direccion() == null && c.ciudad() == null) {
                throw new ValidacionException("SIN_CAMBIOS", "Indica al menos un dato a actualizar");
            }
            Usuario usuario = Busquedas.usuario(usuarios, c.usuarioId());
            PacientePerfil perfil = Busquedas.perfil(perfiles, c.usuarioId());
            if (c.correo() != null) {
                String nuevoCorreo = NormalizadorCorreo.normalizar(c.correo());
                if (!nuevoCorreo.equals(usuario.correo())) {
                    if (usuarios.existePorCorreo(nuevoCorreo)) {
                        throw new ConflictoException("CORREO_YA_REGISTRADO", "El correo ya esta registrado");
                    }
                    usuario = usuarios.guardar(usuario.conCorreo(nuevoCorreo, reloj.ahora()));
                }
            }
            perfil = perfiles.guardar(perfil.actualizarContacto(c.telefono(), c.direccion(), c.ciudad()));
            return new PacienteConCuenta(usuario, perfil);
        });
    }

    /** CU-07: proceso administrativo. */
    public PacienteConCuenta corregirDatosInmutables(CorregirDatosInmutablesComando c) {
        return transacciones.ejecutar(() -> {
            if (c.cedula() == null && c.nombres() == null && c.apellidos() == null && c.fechaNacimiento() == null) {
                throw new ValidacionException("SIN_CAMBIOS", "Indica al menos un dato a corregir");
            }
            String nacimiento = c.fechaNacimiento() == null ? null : Fechas.nacimientoValido(c.fechaNacimiento(), reloj.hoy());
            Usuario usuario = Busquedas.usuario(usuarios, c.pacienteUsuarioId());
            PacientePerfil perfil = Busquedas.perfil(perfiles, c.pacienteUsuarioId());
            String cedula = c.cedula() == null ? null : c.cedula().trim();
            if (cedula != null && !cedula.equals(perfil.cedula()) && perfiles.buscarPorCedula(cedula) != null) {
                throw new ConflictoException("CEDULA_YA_REGISTRADA", "La cedula ya esta registrada");
            }
            PacientePerfil corregido = perfiles.guardar(perfil.corregirIdentidad(
                    cedula,
                    c.nombres() == null ? null : c.nombres().trim(),
                    c.apellidos() == null ? null : c.apellidos().trim(),
                    nacimiento));
            // Se registra quien corrigio y que campos, nunca los valores (RNF-05).
            registro.info("Identidad de paciente corregida por administrativo " + c.actorId() + " sobre usuario " + c.pacienteUsuarioId()
                    + ": cedula=" + (cedula != null) + ", nombres=" + (c.nombres() != null) + ", apellidos=" + (c.apellidos() != null)
                    + ", fechaNacimiento=" + (nacimiento != null));
            return new PacienteConCuenta(usuario, corregido);
        });
    }

    /** Nombres de hasta 50 pacientes, para que profesionales y administrativos los vean junto a sus citas e historias. */
    public NombrePaciente[] consultarNombresPacientes(String[] usuarioIds) {
        return transacciones.ejecutarSoloLectura(() -> {
            ReglasUsuarios.validarLoteDeIds(usuarioIds);
            PacientePerfil[] encontrados = perfiles.buscarPorUsuarioIds(usuarioIds);
            NombrePaciente[] nombres = new NombrePaciente[encontrados.length];
            for (int i = 0; i < encontrados.length; i++) {
                nombres[i] = new NombrePaciente(encontrados[i].usuarioId(), encontrados[i].nombres(), encontrados[i].apellidos());
            }
            return nombres;
        });
    }

    /** Consulta interna para notification (CU-33): el correo de una cuenta activa. Lanza NoEncontradoException si no existe o esta inactiva. */
    public String consultarContacto(String usuarioId) {
        return transacciones.ejecutarSoloLectura(() -> {
            Usuario usuario = usuarios.buscarPorId(usuarioId);
            if (usuario == null || !usuario.activo()) {
                throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
            }
            return usuario.correo();
        });
    }
}
