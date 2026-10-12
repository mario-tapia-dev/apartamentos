package com.blue.apartamentos.enums;

public enum EstadoReservacion {
    PENDIENTE,    // creada, esperando pago/confirmación
    CONFIRMADA,   // confirmada (ya hay un pago confirmado o se confirmó manualmente)
    ACTIVA,       // el cliente ya hizo check-in
    FINALIZADA,   // el cliente ya salió (se llenó fecha_salida)
    CANCELADA
}
