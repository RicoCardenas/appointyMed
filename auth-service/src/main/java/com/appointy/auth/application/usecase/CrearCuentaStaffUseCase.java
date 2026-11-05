package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import com.appointy.auth.domain.rule.PoliticaClave;
import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CrearCuentaStaffUseCase implements CrearCuentaStaffPort {

    private final UsuarioRepositoryPort usuarios;
    private final CodificadorClavePort codificador;
    private final Clock reloj;

    public CrearCuentaStaffUseCase(UsuarioRepositoryPort usuarios, CodificadorClavePort codificador, Clock reloj) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Usuario ejecutar(Comando comando) {
        if (comando.rol() != Rol.PROFESIONAL && comando.rol() != Rol.ADMINISTRATIVO) {
            throw new ValidacionException("ROL_NO_PERMITIDO", "Solo se crean cuentas PROFESIONAL o ADMINISTRATIVO");
        }
        String correo = NormalizadorCorreo.normalizar(comando.correo());
        PoliticaClave.validar(comando.clave());
        if (usuarios.existePorCorreo(correo)) {
            throw new ConflictoException("CORREO_YA_REGISTRADO", "El correo ya esta registrado");
        }
        return usuarios.guardar(
                Usuario.nuevo(UUID.randomUUID(), correo, codificador.codificar(comando.clave()), comando.rol(), reloj.instant()));
    }
}
