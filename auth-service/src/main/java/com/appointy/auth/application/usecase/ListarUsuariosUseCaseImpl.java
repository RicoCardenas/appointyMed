package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.rule.ReglasUsuarios;
import com.appointy.auth.domain.usecase.ListarUsuariosUseCase;

public class ListarUsuariosUseCaseImpl implements ListarUsuariosUseCase {

    private final UsuarioRepositoryGateway usuarios;

    public ListarUsuariosUseCaseImpl(UsuarioRepositoryGateway usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public PaginaUsuarios ejecutar(FiltroUsuarios filtro, int pagina, int tamano) {
        ReglasUsuarios.validarPaginacion(pagina, tamano);
        return usuarios.listar(filtro, pagina, tamano);
    }
}
