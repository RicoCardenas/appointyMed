package com.appointy.auth.infrastructure.config;

import com.appointy.auth.domain.usecase.CuentaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Crea el primer administrador desde ADMIN_EMAIL y ADMIN_PASSWORD si todavia no existe ninguno. */
@Component
public class AdminSeedRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final AdminProperties propiedades;
    private final CuentaUseCase cuentas;

    public AdminSeedRunner(AdminProperties propiedades, CuentaUseCase cuentas) {
        this.propiedades = propiedades;
        this.cuentas = cuentas;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (esVacio(propiedades.email()) || esVacio(propiedades.password())) {
            return;
        }
        if (cuentas.sembrarAdministrador(new CuentaUseCase.SembrarAdministradorComando(propiedades.email(), propiedades.password()))) {
            LOG.info("Administrador inicial creado");
        }
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
