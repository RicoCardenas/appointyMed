package com.appointy.auth.domain.rule;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;

public final class ReglasUsuarios {

    public static final int TAMANO_MAXIMO_PAGINA = 100;
    public static final int MAXIMO_IDS_POR_LOTE = 50;

    private ReglasUsuarios() {}

    public static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ValidacionException("PAGINA_INVALIDA", "La pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new ValidacionException("TAMANO_INVALIDO", "El tamano debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA);
        }
    }

    /** Un administrativo no puede desactivar su propia cuenta: la clinica podria quedarse sin nadie que gestione cuentas. */
    public static void validarDesactivacion(String actorId, String usuarioId) {
        if (actorId.equals(usuarioId)) {
            throw new ConflictoException("NO_AUTODESACTIVAR", "No puedes desactivar tu propia cuenta");
        }
    }

    public static void validarCambioDeClave(String claveActual, String claveNueva) {
        PoliticaClave.validar(claveNueva);
        if (claveNueva.equals(claveActual)) {
            throw new ValidacionException("CLAVE_IGUAL", "La clave nueva debe ser distinta de la actual");
        }
    }

    public static void validarLoteDeIds(String[] ids) {
        if (ids == null || ids.length == 0 || ids.length > MAXIMO_IDS_POR_LOTE) {
            throw new ValidacionException("LOTE_INVALIDO", "Se piden entre 1 y " + MAXIMO_IDS_POR_LOTE + " ids");
        }
    }
}
