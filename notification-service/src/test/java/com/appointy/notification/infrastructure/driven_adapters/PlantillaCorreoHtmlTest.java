package com.appointy.notification.infrastructure.driven_adapters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appointy.notification.infrastructure.driven_adapters.mail.PlantillaCorreoHtml;
import org.junit.jupiter.api.Test;

class PlantillaCorreoHtmlTest {

    static final String CUERPO = "Hola,\n\nTu cita quedó confirmada.\n\nFecha: miércoles 2 de diciembre de 2026\nHora: 8:30 a. m.\nCódigo de la cita: 1FD9F85C\n\n"
            + "Si necesitas reprogramarla, ingresa a Appointy Med.\n\nEste mensaje es automático; por favor no respondas a este correo.";

    @Test
    void generaUnDocumentoHtmlCompletoConMarcaTitularYDetalles() {
        String html = PlantillaCorreoHtml.generar("Cita confirmada: mié 2 dic", CUERPO, "https://front.test");
        assertTrue(html.startsWith("<!doctype html>"), html);
        assertTrue(html.contains("lang=\"es\"") && html.contains("charset=\"utf-8\""), html);
        assertTrue(html.contains("Appointy Med"), html);
        assertTrue(html.contains("<h1") && html.contains("Tu cita quedó confirmada."), html);
        assertTrue(html.contains("Fecha") && html.contains("miércoles 2 de diciembre de 2026"), html);
        assertTrue(html.contains("<table"), "los pares etiqueta: valor van en tabla");
        assertTrue(html.contains("Código de la cita") && html.contains("1FD9F85C"), html);
        assertTrue(html.contains("<title>Cita confirmada: mié 2 dic</title>"), html);
    }

    @Test
    void elBotonApuntaAMisCitasDelFrontSinDoblesBarras() {
        String html = PlantillaCorreoHtml.generar("a", CUERPO, "https://front.test/");
        assertTrue(html.contains("href=\"https://front.test/mis-citas\""), html);
        assertTrue(html.contains("Ver mis citas"), html);
        assertTrue(html.indexOf("Ver mis citas") < html.indexOf("no respondas"), "la nota final va despues del boton");
    }

    @Test
    void sinUrlDelFrontNoHayBoton() {
        assertFalse(PlantillaCorreoHtml.generar("a", CUERPO, "").contains("Ver mis citas"));
        assertFalse(PlantillaCorreoHtml.generar("a", CUERPO, null).contains("Ver mis citas"));
        assertFalse(PlantillaCorreoHtml.generar("a", CUERPO, "   ").contains("Ver mis citas"));
    }

    @Test
    void escapaElHtmlDelContenido() {
        String html = PlantillaCorreoHtml.generar("<b>x</b>", "Hola,\n\n<script>alert(1)</script> & \"comillas\"\n\nFecha: <i>x</i>", "");
        assertFalse(html.contains("<script>"), html);
        assertFalse(html.contains("<i>x</i>"), html);
        assertTrue(html.contains("&lt;script&gt;") && html.contains("&amp;") && html.contains("&quot;comillas&quot;"), html);
        assertTrue(html.contains("<title>&lt;b&gt;x&lt;/b&gt;</title>"), html);
    }

    @Test
    void unCuerpoSimpleSeMuestraComoParrafos() {
        String html = PlantillaCorreoHtml.generar("s", "Una sola linea sin etiquetas", "");
        assertTrue(html.contains("Una sola linea sin etiquetas"), html);
        assertFalse(html.contains("<h1"), "sin segundo parrafo no hay titular");
    }
}
