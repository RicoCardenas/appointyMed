package com.appointy.catalog.domain.model;

public record Centro(String id, String nombre, String direccion, String ciudad, String telefono, boolean activo) {

    public Centro actualizar(String nuevoNombre, String nuevaDireccion, String nuevaCiudad, String nuevoTelefono) {
        return new Centro(id, nuevoNombre, nuevaDireccion, nuevaCiudad, nuevoTelefono, activo);
    }

    public Centro desactivar() {
        return new Centro(id, nombre, direccion, ciudad, telefono, false);
    }
}
