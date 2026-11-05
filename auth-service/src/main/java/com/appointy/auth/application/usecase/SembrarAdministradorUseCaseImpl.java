package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.usecase.CrearCuentaStaffUseCase;
import com.appointy.auth.domain.usecase.SembrarAdministradorUseCase;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import org.springframework.transaction.annotation.Transactional;

public class SembrarAdministradorUseCaseImpl implements SembrarAdministradorUseCase {

    private final UsuarioRepositoryGateway usuarios;
    private final CrearCuentaStaffUseCase crearCuenta;

    public SembrarAdministradorUseCaseImpl(UsuarioRepositoryGateway usuarios, CrearCuentaStaffUseCase crearCuenta) {
        this.usuarios = usuarios;
        this.crearCuenta = crearCuenta;
    }

    @Override
    @Transactional
    public boolean ejecutar(Comando comando) {
        if (usuarios.existeAlgunoConRol(Rol.ADMINISTRATIVO)) {
            return false;
        }
        crearCuenta.ejecutar(new CrearCuentaStaffUseCase.Comando(comando.correo(), comando.clave(), Rol.ADMINISTRATIVO));
        return true;
    }
}
