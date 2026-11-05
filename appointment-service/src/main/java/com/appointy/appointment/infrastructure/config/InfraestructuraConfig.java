package com.appointy.appointment.infrastructure.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties({JwtProperties.class, InternalProperties.class, CatalogProperties.class, NotificationProperties.class})
public class InfraestructuraConfig {

    /**
     * Ejecutor de las tareas @Async (la notificacion posterior al commit). Copia el identificador de correlacion al hilo
     * asincrono para que el log y la llamada a notification conserven el mismo id (RNF-17).
     */
    @Bean(name = "applicationTaskExecutor")
    ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(2);
        ejecutor.setMaxPoolSize(4);
        ejecutor.setQueueCapacity(200);
        ejecutor.setThreadNamePrefix("notif-");
        ejecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ejecutor.setTaskDecorator(tarea -> {
            Map<String, String> contexto = MDC.getCopyOfContextMap();
            return () -> {
                if (contexto != null) {
                    MDC.setContextMap(contexto);
                }
                try {
                    tarea.run();
                } finally {
                    MDC.clear();
                }
            };
        });
        return ejecutor;
    }
}
