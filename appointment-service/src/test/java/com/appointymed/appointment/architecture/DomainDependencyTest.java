package com.appointymed.appointment.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainDependencyTest {
    @Test
    void domainHasNoFrameworkOrJavaImports() throws IOException {
        List<String> forbidden = List.of("import java.", "import javax.", "import jakarta.", "import org.", "import net.", "import software.");
        try (var files = Files.walk(Path.of("src/main/java/com/appointymed/appointment/domain"))) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    for (String prefix : forbidden) assertThat(source).as(path.toString()).doesNotContain(prefix);
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
        }
    }
}
