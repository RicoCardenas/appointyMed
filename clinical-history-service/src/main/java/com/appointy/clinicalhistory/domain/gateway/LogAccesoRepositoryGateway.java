package com.appointy.clinicalhistory.domain.gateway;

import com.appointy.clinicalhistory.domain.model.LogAcceso;

/** Solo inserta (RF-30). Se une a la transaccion en curso si la hay; si no, se guarda en la suya. */
public interface LogAccesoRepositoryGateway {

    void registrar(LogAcceso log);
}
