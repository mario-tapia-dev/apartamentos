package com.blue.apartamentos.enums;

public enum EstadoPago {
    PENDIENTE,
    NO_RECIBIDO,
    RECIBIDO,
    CONFIRMADO,
    CANCELADO;

    /** Transiciones permitidas del flujo de un pago. CONFIRMADO y CANCELADO son estados finales. */
    public boolean puedePasarA(EstadoPago nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == RECIBIDO || nuevo == NO_RECIBIDO || nuevo == CANCELADO;
            case NO_RECIBIDO -> nuevo == RECIBIDO || nuevo == CANCELADO;
            case RECIBIDO -> nuevo == CONFIRMADO || nuevo == NO_RECIBIDO || nuevo == CANCELADO;
            case CONFIRMADO, CANCELADO -> false;
        };
    }
}
