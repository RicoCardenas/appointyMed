package com.appointy.auth.domain.model;

/** Cualquier campo en null significa "sin filtrar por ese campo". */
public record FiltroUsuarios(Rol rol, Boolean activo) {}
