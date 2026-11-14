package com.appointy.catalog.domain.model;

/** {@code texto}: fragmento de nombres, apellidos o registro medico. Cualquier campo en null significa "sin filtrar". */
public record FiltroProfesionales(String texto, Boolean activo) {}
