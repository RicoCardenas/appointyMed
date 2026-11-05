package com.appointy.catalog.domain.model;

public record Especialidad(String id, String nombre, String descripcion, boolean activo) {

    public Especialidad actualizar(String nuevoNombre, String nuevaDescripcion) {
        return new Especialidad(id, nuevoNombre, nuevaDescripcion, activo);
    }

    public Especialidad desactivar() {
        return new Especialidad(id, nombre, descripcion, false);
    }
}
