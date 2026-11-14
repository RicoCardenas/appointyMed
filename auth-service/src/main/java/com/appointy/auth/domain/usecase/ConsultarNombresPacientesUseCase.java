package com.appointy.auth.domain.usecase;

import com.appointy.auth.domain.model.NombrePaciente;

/** Nombres de hasta 50 pacientes, para que profesionales y administrativos los vean junto a sus citas e historias. */
public interface ConsultarNombresPacientesUseCase {

    NombrePaciente[] ejecutar(String[] usuarioIds);
}
