package com.appointy.clinicalhistory.infrastructure.entry_points.dto;

import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import java.time.Instant;

public record ResumenHistoriaResponse(int atenciones, int correcciones, Instant ultimaAtencionEn, String ultimoDiagnostico) {

    public static ResumenHistoriaResponse de(ResumenHistoria r) {
        return new ResumenHistoriaResponse(r.atenciones(), r.correcciones(), r.ultimaAtencionEn() == 0 ? null : Instant.ofEpochMilli(r.ultimaAtencionEn()),
                r.ultimoDiagnostico());
    }
}
