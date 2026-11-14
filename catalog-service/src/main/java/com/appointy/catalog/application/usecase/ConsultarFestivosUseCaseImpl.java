package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.rule.ReglasConsulta;
import com.appointy.catalog.domain.usecase.ConsultarFestivosUseCase;

public class ConsultarFestivosUseCaseImpl implements ConsultarFestivosUseCase {

    private final FestivosClientGateway festivos;

    public ConsultarFestivosUseCaseImpl(FestivosClientGateway festivos) {
        this.festivos = festivos;
    }

    @Override
    public Festivo[] ejecutar(int anio) {
        ReglasConsulta.validarAnio(anio);
        return festivos.festivos(anio);
    }
}
