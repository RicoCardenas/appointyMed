package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.GeneradorIdGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import com.appointy.auth.domain.gateway.TransactionGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import com.appointy.auth.domain.rule.PoliticaClave;
import com.appointy.auth.domain.rule.ReglasUsuarios;

/** Cuentas: registro (CU-01), cuentas de personal (CU-06), clave, estado y consultas administrativas (RA-04). */
public class CuentaUseCase {

    /** {@code fechaNacimiento}: texto ISO yyyy-MM-dd. */
    public record RegistrarPacienteComando(String correo, String clave, String cedula, String nombres, String apellidos,
            String fechaNacimiento, String telefono, String direccion, String ciudad) {}

    public record CrearCuentaStaffComando(String correo, String clave, Rol rol) {}

    public record SembrarAdministradorComando(String correo, String clave) {}

    public record CambiarClaveComando(String usuarioId, String claveActual, String claveNueva) {}

    public record CambiarEstadoCuentaComando(String actorId, String usuarioId, boolean activo) {}

    private final UsuarioRepositoryGateway usuarios;
    private final PacientePerfilRepositoryGateway perfiles;
    private final CodificadorClaveGateway codificador;
    private final RelojGateway reloj;
    private final GeneradorIdGateway ids;
    private final TransactionGateway transacciones;

    public CuentaUseCase(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, CodificadorClaveGateway codificador,
            RelojGateway reloj, GeneradorIdGateway ids, TransactionGateway transacciones) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.codificador = codificador;
        this.reloj = reloj;
        this.ids = ids;
        this.transacciones = transacciones;
    }

    public PacienteConCuenta registrarPaciente(RegistrarPacienteComando comando) {
        return transacciones.ejecutar(() -> {
            String correo = NormalizadorCorreo.normalizar(comando.correo());
            PoliticaClave.validar(comando.clave());
            String nacimiento = Fechas.nacimientoValido(comando.fechaNacimiento(), reloj.hoy());
            String cedula = comando.cedula().trim();
            validarCorreoLibre(correo);
            if (perfiles.buscarPorCedula(cedula) != null) {
                throw new ConflictoException("CEDULA_YA_REGISTRADA", "La cedula ya esta registrada");
            }
            Usuario usuario = nuevoUsuario(correo, comando.clave(), Rol.PACIENTE);
            PacientePerfil perfil = perfiles.guardar(new PacientePerfil(usuario.id(), cedula, comando.nombres().trim(),
                    comando.apellidos().trim(), nacimiento, comando.telefono(), comando.direccion(), comando.ciudad()));
            return new PacienteConCuenta(usuario, perfil);
        });
    }

    /** CU-06: cuentas de PROFESIONAL o ADMINISTRATIVO. */
    public Usuario crearCuentaStaff(CrearCuentaStaffComando comando) {
        return transacciones.ejecutar(() -> crearStaff(comando));
    }

    /** Crea el primer ADMINISTRATIVO si no existe ninguno. Devuelve true si creo la cuenta. */
    public boolean sembrarAdministrador(SembrarAdministradorComando comando) {
        return transacciones.ejecutar(() -> {
            if (usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)) {
                return false;
            }
            crearStaff(new CrearCuentaStaffComando(comando.correo(), comando.clave(), Rol.ADMINISTRATIVO));
            return true;
        });
    }

    /** Los tokens ya emitidos siguen validos hasta expirar (decision 9). */
    public void cambiarClave(CambiarClaveComando comando) {
        transacciones.ejecutar(() -> {
            ReglasUsuarios.validarCambioDeClave(comando.claveActual(), comando.claveNueva());
            Usuario usuario = Busquedas.usuario(usuarios, comando.usuarioId());
            if (!codificador.coincide(comando.claveActual(), usuario.claveHash())) {
                throw new ValidacionException("CLAVE_ACTUAL_INCORRECTA", "La clave actual no es correcta");
            }
            usuarios.guardar(usuario.conClaveHash(codificador.codificar(comando.claveNueva()), reloj.ahora()));
            return null;
        });
    }

    /** Activa o desactiva una cuenta. Idempotente: repetir la operacion no falla. */
    public Usuario cambiarEstadoCuenta(CambiarEstadoCuentaComando comando) {
        return transacciones.ejecutar(() -> {
            if (!comando.activo()) {
                ReglasUsuarios.validarDesactivacion(comando.actorId(), comando.usuarioId());
            }
            Usuario usuario = Busquedas.usuario(usuarios, comando.usuarioId());
            if (usuario.activo() == comando.activo()) {
                return usuario;
            }
            return usuarios.guardar(usuario.conActivo(comando.activo(), reloj.ahora()));
        });
    }

    public PaginaUsuarios listarUsuarios(FiltroUsuarios filtro, int pagina, int tamano) {
        ReglasUsuarios.validarPaginacion(pagina, tamano);
        return usuarios.listar(filtro, pagina, tamano);
    }

    public EstadisticasUsuarios consultarEstadisticasUsuarios() {
        return usuarios.estadisticas();
    }

    private Usuario crearStaff(CrearCuentaStaffComando comando) {
        if (comando.rol() != Rol.PROFESIONAL && comando.rol() != Rol.ADMINISTRATIVO) {
            throw new ValidacionException("ROL_NO_PERMITIDO", "Solo se crean cuentas PROFESIONAL o ADMINISTRATIVO");
        }
        String correo = NormalizadorCorreo.normalizar(comando.correo());
        PoliticaClave.validar(comando.clave());
        validarCorreoLibre(correo);
        return nuevoUsuario(correo, comando.clave(), comando.rol());
    }

    private void validarCorreoLibre(String correo) {
        if (usuarios.existePorCorreo(correo)) {
            throw new ConflictoException("CORREO_YA_REGISTRADO", "El correo ya esta registrado");
        }
    }

    private Usuario nuevoUsuario(String correo, String clave, Rol rol) {
        return usuarios.guardar(Usuario.nuevo(ids.nuevoId(), correo, codificador.codificar(clave), rol, reloj.ahora()));
    }
}
