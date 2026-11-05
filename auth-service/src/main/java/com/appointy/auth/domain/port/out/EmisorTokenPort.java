package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;

public interface EmisorTokenPort {

    TokenEmitido emitir(Usuario usuario);

    /** Devuelve los datos del token si su firma, emisor y vigencia son validos; null en cualquier otro caso. */
    ClaimsToken validar(String token);
}
