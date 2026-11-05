package com.appointy.appointment.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.exception.ConflictoException;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.usecase.ReprogramarCitaUseCase;
import com.appointy.appointment.domain.usecase.ReservarCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.NotificacionClientGateway;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * RNF-02 y RNF-09 contra PostgreSQL real (no H2: el indice parcial y el bloqueo de filas son de PostgreSQL). Solo corre si
 * APPOINTMENT_DB_PASSWORD esta definida (por ejemplo cargando .env). Catalog y notification se simulan; la base es la real.
 * Cada prueba crea sus propios datos con un profesional nuevo y los borra al terminar.
 */
@SpringBootTest(properties = {
    "app.jwt.secret=clave-de-prueba-de-concurrencia-0123456789abcdef",
    "app.internal.api-key=llave-de-prueba-de-24-caracteres-o-mas"})
@EnabledIfEnvironmentVariable(named = "APPOINTMENT_DB_PASSWORD", matches = ".+")
class ReservaConcurrenteTest {

    static final int HILOS = 12;

    @Autowired ReservarCitaUseCase reservar;
    @Autowired ReprogramarCitaUseCase reprogramar;
    @Autowired FranjaRepositoryGateway franjas;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate tx;
    @MockitoBean CatalogClientGateway catalog;
    @MockitoBean NotificacionClientGateway notificacion;

    final String profesionalId = UUID.randomUUID().toString();
    final String centroId = UUID.randomUUID().toString();
    final String especialidadId = UUID.randomUUID().toString();
    final String fecha = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(5).toString();

    @BeforeEach
    void preparar() {
        when(catalog.obtenerParametros()).thenReturn(new ParametrosCita(2, 24, 60));
        when(catalog.validarAsociacion(anyString(), anyString(), anyString())).thenReturn(new AsociacionProfesional(true, true));
    }

    @AfterEach
    void limpiar() {
        jdbc.update("delete from historial_cita where cita_id in (select id from cita where profesional_id = ?::uuid)", profesionalId);
        jdbc.update("delete from cita where profesional_id = ?::uuid", profesionalId);
        jdbc.update("delete from franja where profesional_id = ?::uuid", profesionalId);
    }

    String crearFranja(int horaInicio) {
        String id = UUID.randomUUID().toString();
        tx.executeWithoutResult(s -> franjas.insertarSiNoExiste(
                new Franja(id, profesionalId, centroId, fecha, horaInicio, horaInicio + 30, EstadoFranja.DISPONIBLE, null)));
        return id;
    }

    ReservarCitaUseCase.Comando reserva(String paciente, String franjaId) {
        return new ReservarCitaUseCase.Comando(new Actor(paciente, "PACIENTE"), especialidadId, profesionalId, centroId, franjaId);
    }

    /** Suelta todas las tareas a la vez y devuelve el resultado de cada una: null si tuvo exito, o la excepcion. */
    static List<Throwable> correrJuntas(List<Callable<Object>> tareas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tareas.size());
        CountDownLatch listos = new CountDownLatch(tareas.size());
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Throwable>> futuros = new ArrayList<>();
        for (Callable<Object> tarea : tareas) {
            futuros.add(pool.submit(() -> {
                listos.countDown();
                salida.await();
                try {
                    tarea.call();
                    return null;
                } catch (Throwable e) {
                    return e;
                }
            }));
        }
        listos.await();
        salida.countDown();
        List<Throwable> resultados = new ArrayList<>();
        for (Future<Throwable> f : futuros) {
            resultados.add(f.get(60, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return resultados;
    }

    @Test
    void docePacientesReservandoLaMismaFranjaSoloUnoGana() throws Exception {
        String franja = crearFranja(480);
        List<Callable<Object>> tareas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            String paciente = UUID.randomUUID().toString();
            tareas.add(() -> reservar.ejecutar(reserva(paciente, franja)));
        }

        List<Throwable> resultados = correrJuntas(tareas);

        long exitos = resultados.stream().filter(r -> r == null).count();
        assertEquals(1, exitos, "exactamente una reserva debe tener exito: " + resultados);
        resultados.stream().filter(r -> r != null).forEach(r -> assertInstanceOf(ConflictoException.class, r, "los demas reciben un conflicto controlado"));
        assertEquals(1, jdbc.queryForObject("select count(*) from cita where franja_id = ?::uuid and estado = 'CONFIRMADA'", Integer.class, franja));
        assertEquals("OCUPADA", jdbc.queryForObject("select estado from franja where id = ?::uuid", String.class, franja));
        assertEquals(1, jdbc.queryForObject("select count(*) from historial_cita h join cita c on c.id = h.cita_id where c.franja_id = ?::uuid", Integer.class, franja));
    }

    @Test
    void elMismoPacienteNoPuedeReservarAlMismoTiempoDosFranjasQueSeCruzan() throws Exception {
        String f1 = crearFranja(480);
        String f2 = crearFranja(495);  // se cruza con la anterior (08:00-08:30 y 08:15-08:45)
        String paciente = UUID.randomUUID().toString();

        List<Throwable> resultados = correrJuntas(List.of(() -> reservar.ejecutar(reserva(paciente, f1)), () -> reservar.ejecutar(reserva(paciente, f2))));

        assertEquals(1, resultados.stream().filter(r -> r == null).count(), "solo una de las dos citas cruzadas: " + resultados);
        resultados.stream().filter(r -> r != null).forEach(r -> assertEquals("CRUCE_DE_CITAS", ((ConflictoException) r).codigo()));
        assertEquals(1, jdbc.queryForObject("select count(*) from cita where paciente_id = ?::uuid and estado = 'CONFIRMADA'", Integer.class, paciente));
    }

    @Test
    void reprogramacionesCruzadasTerminanSinDeadlockYSinCambios() throws Exception {
        String f1 = crearFranja(480);
        String f2 = crearFranja(600);
        String pacienteA = UUID.randomUUID().toString();
        String pacienteB = UUID.randomUUID().toString();
        String citaA = reservar.ejecutar(reserva(pacienteA, f1)).cita().id();
        String citaB = reservar.ejecutar(reserva(pacienteB, f2)).cita().id();

        // A pide la franja de B y B la de A, a la vez: ambas estan OCUPADAS, asi que ambas deben rechazarse sin deadlock.
        List<Throwable> resultados = correrJuntas(List.of(
                () -> reprogramar.ejecutar(new ReprogramarCitaUseCase.Comando(new Actor(pacienteA, "PACIENTE"), citaA, f2)),
                () -> reprogramar.ejecutar(new ReprogramarCitaUseCase.Comando(new Actor(pacienteB, "PACIENTE"), citaB, f1))));

        resultados.forEach(r -> assertInstanceOf(ConflictoException.class, r, "rechazo controlado, no deadlock: " + r));
        assertEquals(f1, jdbc.queryForObject("select franja_id::text from cita where id = ?::uuid", String.class, citaA));
        assertEquals(f2, jdbc.queryForObject("select franja_id::text from cita where id = ?::uuid", String.class, citaB));
        assertEquals(2, jdbc.queryForObject("select count(*) from franja where profesional_id = ?::uuid and estado = 'OCUPADA'", Integer.class, profesionalId));
    }

    @Test
    void dosReprogramacionesHaciaLaMismaFranjaLibreSoloUnaGana() throws Exception {
        String f1 = crearFranja(480);
        String f2 = crearFranja(600);
        String libre = crearFranja(720);
        String pacienteA = UUID.randomUUID().toString();
        String pacienteB = UUID.randomUUID().toString();
        String citaA = reservar.ejecutar(reserva(pacienteA, f1)).cita().id();
        String citaB = reservar.ejecutar(reserva(pacienteB, f2)).cita().id();

        List<Throwable> resultados = correrJuntas(List.of(
                () -> reprogramar.ejecutar(new ReprogramarCitaUseCase.Comando(new Actor(pacienteA, "PACIENTE"), citaA, libre)),
                () -> reprogramar.ejecutar(new ReprogramarCitaUseCase.Comando(new Actor(pacienteB, "PACIENTE"), citaB, libre))));

        assertEquals(1, resultados.stream().filter(r -> r == null).count(), "una gana y la otra recibe conflicto: " + resultados);
        assertEquals(1, jdbc.queryForObject("select count(*) from cita where franja_id = ?::uuid and estado = 'CONFIRMADA'", Integer.class, libre));
        // La que perdio conserva su franja original y la ganadora libero la suya.
        assertEquals(2, jdbc.queryForObject("select count(*) from franja where profesional_id = ?::uuid and estado = 'OCUPADA'", Integer.class, profesionalId));
        assertEquals(1, jdbc.queryForObject("select count(*) from franja where profesional_id = ?::uuid and estado = 'DISPONIBLE'", Integer.class, profesionalId));
    }

    @Test
    void elIndiceUnicoParcialRechazaDosCitasConfirmadasEnLaMismaFranjaAunSinPasarPorLaAplicacion() {
        String franja = crearFranja(480);
        String insertar = "insert into cita (id, paciente_id, profesional_id, especialidad_id, centro_id, franja_id, estado, creado_en, modificado_en) "
                + "values (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?, now(), now())";
        jdbc.update(insertar, UUID.randomUUID().toString(), UUID.randomUUID().toString(), profesionalId, especialidadId, centroId, franja, "CONFIRMADA");

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                insertar, UUID.randomUUID().toString(), UUID.randomUUID().toString(), profesionalId, especialidadId, centroId, franja, "CONFIRMADA"));
        assertTrue(ex.getMessage().contains("uq_cita_franja_confirmada"));
        // Una cita cancelada en la misma franja si es valida: el indice solo cubre las CONFIRMADAS.
        jdbc.update(insertar, UUID.randomUUID().toString(), UUID.randomUUID().toString(), profesionalId, especialidadId, centroId, franja, "CANCELADA");
    }
}
