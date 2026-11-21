package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.rule.ReglasConsulta;
import com.appointy.catalog.domain.rule.ReglasParametro;

/** configurar parametros, consultar parametros, consultar festivos. */
public class ParametroUseCase {

    public record ConfigurarParametrosComando(ParametroServicio[] parametros) {}

    private final ParametroRepositoryGateway parametros;
    private final FestivosClientGateway festivos;
    private final TransactionGateway transacciones;

    public ParametroUseCase(ParametroRepositoryGateway parametros, FestivosClientGateway festivos, TransactionGateway transacciones) {
        this.transacciones = transacciones;
        this.parametros = parametros;
        this.festivos = festivos;
    }

    /** CU-15: actualiza uno o varios parametros; solo cuentan clave y valor. */
    public ParametroServicio[] configurarParametros(ConfigurarParametrosComando c) {
        return transacciones.ejecutar(() -> {
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
        });
    }

    /** Parametros del servicio. */
    public ParametroServicio[] consultarParametros() {
        return transacciones.ejecutarSoloLectura(() -> {
            return parametros.listar();
        });
    }

    /** Festivos del anio segun la API externa (RA-06). Lanza ServicioNoDisponibleException si no responde. */
    public Festivo[] consultarFestivos(int anio) {
        ReglasConsulta.validarAnio(anio);
        return festivos.festivos(anio);
    }
}
