package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort;
import com.appointy.auth.domain.port.in.SembrarAdministradorPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class SembrarAdministradorUseCase implements SembrarAdministradorPort {

    private final UsuarioRepositoryPort usuarios;
    private final CrearCuentaStaffPort crearCuenta;

    public SembrarAdministradorUseCase(UsuarioRepositoryPort usuarios, CrearCuentaStaffPort crearCuenta) {
        this.usuarios = usuarios;
        this.crearCuenta = crearCuenta;
    }

    @Override
    @Transactional
    public boolean ejecutar(Comando comando) {
        if (usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)) {
            return false;
        }
        crearCuenta.ejecutar(new CrearCuentaStaffPort.Comando(comando.correo(), comando.clave(), Rol.ADMINISTRATIVO));
        return true;
    }
}
