package com.appointy.notification.infrastructure.driven_adapters.sistema;

import com.appointy.notification.domain.gateway.RegistroGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4jRegistroAdapter implements RegistroGateway {

    private static final Logger LOG = LoggerFactory.getLogger("notification");

    @Override
    public void info(String mensaje) {
        LOG.info("{}", mensaje);
    }

    @Override
    public void error(String mensaje) {
        LOG.error("{}", mensaje);
    }
}
