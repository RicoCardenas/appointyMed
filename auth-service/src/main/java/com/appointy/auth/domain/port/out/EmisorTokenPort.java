package com.appointy.auth.domain.port.out;

import com.appointy.auth.domain.model.ClaimsToken;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;
import java.util.Optional;

public interface EmisorTokenPort {

    TokenEmitido emitir(Usuario usuario);

    /** Devuelve los datos del token si su firma, emisor y vigencia son validos; vacio en cualquier otro caso. */
    Optional<ClaimsToken> validar(String token);
}
