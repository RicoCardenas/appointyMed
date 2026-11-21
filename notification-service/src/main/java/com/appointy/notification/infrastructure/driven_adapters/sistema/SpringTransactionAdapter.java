package com.appointy.notification.infrastructure.driven_adapters.sistema;

import com.appointy.notification.domain.gateway.Operacion;
import com.appointy.notification.domain.gateway.TransactionGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringTransactionAdapter implements TransactionGateway {

    private final TransactionTemplate normal;
    private final TransactionTemplate lectura;

    public SpringTransactionAdapter(PlatformTransactionManager manager) {
        this.normal = new TransactionTemplate(manager);
        this.lectura = new TransactionTemplate(manager);
        this.lectura.setReadOnly(true);
    }

    @Override
    public <T> T ejecutar(Operacion<T> operacion) {
        return normal.execute(estado -> operacion.ejecutar());
    }

    @Override
    public <T> T ejecutarSoloLectura(Operacion<T> operacion) {
        return lectura.execute(estado -> operacion.ejecutar());
    }
}
