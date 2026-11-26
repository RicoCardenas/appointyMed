package com.appointy.notification.infrastructure.driven_adapters.mail;

/**
 * Arma el correo HTML a partir del cuerpo de texto que genera el dominio. Convencion del cuerpo: parrafos separados por una
 * linea en blanco; el primero es el saludo, el segundo el titular, un parrafo cuyas lineas son todas "Etiqueta: valor" se muestra
 * como tabla de detalles y el ultimo es la nota final. Maquetacion con tablas y estilos en linea, que es lo que soportan los
 * clientes de correo. Todo el contenido se escapa.
 */
public final class PlantillaCorreoHtml {

    private static final String TEAL = "#0b6e72";
    private static final String TINTA = "#1c2b30";
    private static final String APAGADO = "#566a70";
    private static final String LINEA = "#dbe5e6";

    private PlantillaCorreoHtml() {}

    public static String generar(String asunto, String cuerpo, String urlFront) {
        String[] parrafos = cuerpo.split("\n\n");
        StringBuilder contenido = new StringBuilder();
        String pie = "";
        for (int i = 0; i < parrafos.length; i++) {
            String p = parrafos[i];
            boolean ultimo = i == parrafos.length - 1 && parrafos.length > 2;
            if (i == 1 && parrafos.length > 2) {
                contenido.append("<h1 style=\"margin:0 0 20px;font-size:22px;line-height:1.3;color:").append(TINTA).append("\">")
                        .append(escapar(p)).append("</h1>");
            } else if (esDetalle(p)) {
                contenido.append(tabla(p));
            } else if (ultimo) {
                pie = "<p style=\"margin:28px 0 0;font-size:13px;line-height:1.5;color:" + APAGADO + "\">" + escapar(p) + "</p>";
            } else {
                contenido.append("<p style=\"margin:0 0 16px;font-size:16px;line-height:1.6;color:").append(TINTA).append("\">")
                        .append(escapar(p).replace("\n", "<br>")).append("</p>");
            }
        }
        return "<!doctype html><html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<meta name=\"color-scheme\" content=\"light\"><title>" + escapar(asunto) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f3f7f7;font-family:-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f3f7f7\"><tr><td align=\"center\" style=\"padding:24px 12px\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:560px;background:#ffffff;border:1px solid " + LINEA + ";border-radius:12px;overflow:hidden\">"
                + "<tr><td style=\"background:" + TEAL + ";padding:18px 28px;color:#ffffff;font-size:18px;font-weight:700\">Appointy Med</td></tr>"
                + "<tr><td style=\"padding:28px\">" + contenido + boton(urlFront) + pie + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private static boolean esDetalle(String parrafo) {
        for (String linea : parrafo.split("\n")) {
            if (!linea.contains(": ")) {
                return false;
            }
        }
        return true;
    }

    private static String tabla(String parrafo) {
        StringBuilder t = new StringBuilder("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 20px;border:1px solid "
                + LINEA + ";border-radius:8px;background:#f8fbfb\">");
        for (String linea : parrafo.split("\n")) {
            int corte = linea.indexOf(": ");
            t.append("<tr><td style=\"padding:10px 16px;font-size:14px;color:").append(APAGADO).append(";width:38%;border-bottom:1px solid ").append(LINEA).append("\">")
                    .append(escapar(linea.substring(0, corte))).append("</td><td style=\"padding:10px 16px;font-size:16px;font-weight:600;color:")
                    .append(TINTA).append(";border-bottom:1px solid ").append(LINEA).append("\">")
                    .append(escapar(linea.substring(corte + 2))).append("</td></tr>");
        }
        return t.append("</table>").toString();
    }

    private static String boton(String urlFront) {
        if (urlFront == null || urlFront.isBlank()) {
            return "";
        }
        String base = urlFront.trim();
        String url = (base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + "/mis-citas";
        return "<p style=\"margin:8px 0 0\"><a href=\"" + escapar(url) + "\" style=\"display:inline-block;background:" + TEAL
                + ";color:#ffffff;text-decoration:none;font-weight:600;font-size:16px;padding:12px 24px;border-radius:8px\">Ver mis citas</a></p>";
    }

    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
