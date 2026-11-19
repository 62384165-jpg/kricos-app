package com.example.kricos_app.model;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    public Set<EstadoPedido> siguientesPermitidos() {
        switch (this) {
            case PENDIENTE:
                return EnumSet.of(EN_PREPARACION, CANCELADO);
            case EN_PREPARACION:
                return EnumSet.of(LISTO, CANCELADO);
            case LISTO:
                return EnumSet.of(ENTREGADO);
            default:
                return EnumSet.noneOf(EstadoPedido.class);
        }
    }
}
