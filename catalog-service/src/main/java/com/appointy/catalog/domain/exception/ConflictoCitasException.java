package com.appointy.catalog.domain.exception;

/** appointment rechazo el bloqueo porque hay citas CONFIRMADAS en el intervalo; lista cuales (RN-11). */
public class ConflictoCitasException extends ConflictoException {

    private final String[] citasAfectadas;

    public ConflictoCitasException(String[] citasAfectadas) {
        super("CITAS_CONFIRMADAS_EN_INTERVALO",
                "Hay citas confirmadas en el intervalo; reprogramalas o cancelalas primero",
                citasAfectadas == null ? new String[0] : citasAfectadas);
        this.citasAfectadas = citasAfectadas == null ? new String[0] : citasAfectadas;
    }

    public String[] citasAfectadas() {
        return citasAfectadas;
    }
}
