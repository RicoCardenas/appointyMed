package com.appointy.auth.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** RNF-08: el dominio no conoce frameworks y las dependencias solo apuntan hacia adentro. */
class DependenciasEntreCapasTest {

    private static final Path RAIZ = Path.of("src/main/java/com/appointy/auth");

    private static List<String> importsProhibidos(String capa, List<String> prefijos) throws IOException {
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
            return archivos.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream()
                                    .filter(l -> l.startsWith("import "))
                                    .filter(l -> prefijos.stream().anyMatch(pre -> l.startsWith("import " + pre) || l.startsWith("import static " + pre)))
                                    .map(l -> p.getFileName() + " -> " + l);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();
        }
    }

    @Test
    void elDominioNoImportaFrameworksNiCapasExternas() throws IOException {
        List<String> infracciones = importsProhibidos("domain", List.of(
                "org.springframework", "jakarta.", "org.hibernate", "lombok", "org.mapstruct",
                "com.fasterxml", "io.jsonwebtoken", "org.slf4j",
                "com.appointy.auth.application", "com.appointy.auth.infrastructure"));
        assertTrue(infracciones.isEmpty(), "El dominio importa algo prohibido: " + infracciones);
    }

    @Test
    void laAplicacionNoImportaInfraestructura() throws IOException {
        List<String> infracciones = importsProhibidos("application", List.of("com.appointy.auth.infrastructure", "jakarta.persistence"));
        assertTrue(infracciones.isEmpty(), "La aplicacion importa infraestructura: " + infracciones);
    }
}
