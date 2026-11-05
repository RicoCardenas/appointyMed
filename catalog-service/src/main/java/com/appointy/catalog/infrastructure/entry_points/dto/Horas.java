package com.appointy.catalog.infrastructure.entry_points.dto;

/** La API habla en "HH:mm"; el dominio, en minutos desde medianoche. */
public final class Horas {

    public static final String PATRON = "^([01]\\d|2[0-3]):[0-5]\\d$";

    private Horas() {}

    public static int aMinutos(String hhmm) {
        return Integer.parseInt(hhmm.substring(0, 2)) * 60 + Integer.parseInt(hhmm.substring(3, 5));
    }

    public static String aTexto(int minutos) {
        return String.format("%02d:%02d", minutos / 60, minutos % 60);
    }
}
