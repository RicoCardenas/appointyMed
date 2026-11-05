package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;

/** CU-31: solo el autor corrige; la correccion es una entrada nueva que referencia la anterior (RF-29). */
public interface CorregirEntradaUseCase {

    record Comando(Actor actor, String entradaId, ContenidoClinico contenido) {}

    EntradaClinica ejecutar(Comando comando);
}
