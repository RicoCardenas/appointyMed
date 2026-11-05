package com.appointy.auth.domain.port.in;

import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;

/** CU-06: cuentas de PROFESIONAL o ADMINISTRATIVO. */
public interface CrearCuentaStaffPort {

    Usuario ejecutar(Comando comando);

    record Comando(String correo, String clave, Rol rol) {}
}
