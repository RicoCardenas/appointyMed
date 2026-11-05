package com.appointy.catalog.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** RNF-08: el dominio no depende de nada externo y las dependencias solo apuntan hacia adentro. */
class DependenciasEntreCapasTest {

    private static final Path RAIZ = Path.of("src/main/java/com/appointy/catalog");

    private static List<String> lineas(String capa) throws IOException {
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
            return archivos.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream().map(l -> p.getFileName() + " -> " + l);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();
        }
    }

    /** Regla del docente: en el dominio no entra ninguna libreria, ni siquiera del JDK; solo codigo propio del dominio. */
    @Test
    void elDominioSoloImportaSuPropioPaquete() throws IOException {
        List<String> infracciones = lineas("domain").stream()
                .filter(l -> l.contains("-> import "))
                .filter(l -> !l.contains("-> import com.appointy.catalog.domain."))
                .toList();
        assertTrue(infracciones.isEmpty(), "El dominio importa algo ajeno a si mismo: " + infracciones);
    }

    @Test
    void elDominioNoUsaTiposExternosConNombreCompleto() throws IOException {
        List<String> infracciones = lineas("domain").stream()
                .filter(l -> !l.contains("-> package "))
                .filter(l -> !l.contains("-> import "))
                .filter(l -> !l.contains("*") && !l.contains("//"))
                .filter(l -> l.matches(".*\\b(java|javax|jakarta|org|lombok)\\.[a-z]+\\.[A-Za-z].*"))
                .toList();
        assertTrue(infracciones.isEmpty(), "El dominio usa un tipo externo sin importarlo: " + infracciones);
    }

    @Test
    void laAplicacionNoImportaInfraestructura() throws IOException {
        List<String> infracciones = lineas("application").stream()
                .filter(l -> l.contains("-> import com.appointy.catalog.infrastructure") || l.contains("-> import jakarta.persistence"))
                .toList();
        assertTrue(infracciones.isEmpty(), "La aplicacion importa infraestructura: " + infracciones);
    }
}
