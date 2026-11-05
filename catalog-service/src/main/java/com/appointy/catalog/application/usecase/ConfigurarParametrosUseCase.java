package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.port.in.ConfigurarParametrosPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.rule.ReglasParametro;
import org.springframework.transaction.annotation.Transactional;

public class ConfigurarParametrosUseCase implements ConfigurarParametrosPort {

    private final ParametroRepositoryPort parametros;

    public ConfigurarParametrosUseCase(ParametroRepositoryPort parametros) {
        this.parametros = parametros;
    }

    @Override
    @Transactional
    public ParametroServicio[] ejecutar(Comando c) {
        if (c.parametros() == null || c.parametros().length == 0) {
            throw new ValidacionException("SIN_CAMBIOS", "Indica al menos un parametro");
        }
        for (ParametroServicio p : c.parametros()) {
            ReglasParametro.validar(p.clave(), p.valor());
        }
        for (ParametroServicio p : c.parametros()) {
            ParametroServicio actual = parametros.buscarPorClave(p.clave());
            parametros.guardar(new ParametroServicio(p.clave(), p.valor().trim(), actual != null ? actual.descripcion() : null));
        }
        return parametros.listar();
    }
}
