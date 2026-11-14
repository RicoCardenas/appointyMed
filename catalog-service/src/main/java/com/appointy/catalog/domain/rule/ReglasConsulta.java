package com.appointy.catalog.domain.rule;

import com.appointy.catalog.domain.exception.ValidacionException;

public final class ReglasConsulta {

    public static final int TAMANO_MAXIMO_PAGINA = 100;
    public static final int ANIO_MINIMO = 2000;
    public static final int ANIO_MAXIMO = 2100;

    private ReglasConsulta() {}

    public static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ValidacionException("PAGINA_INVALIDA", "La pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new ValidacionException("TAMANO_INVALIDO", "El tamano debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA);
        }
    }

    public static void validarAnio(int anio) {
        if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new ValidacionException("ANIO_INVALIDO", "El anio debe estar entre " + ANIO_MINIMO + " y " + ANIO_MAXIMO);
        }
    }
}
