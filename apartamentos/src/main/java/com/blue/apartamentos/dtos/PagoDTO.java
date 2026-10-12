package com.blue.apartamentos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.enums.MetodoPago;
import com.blue.apartamentos.models.PagoModel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class PagoDTO {

    private PagoDTO() {
    }

    /** montoTotal es opcional: si no se manda se cobra el saldo pendiente de la reservación. */
    public record Request(
            @NotNull Long idReservacion,
            @NotNull MetodoPago metodo,
            @Positive BigDecimal montoTotal) {
    }

    public record EstadoRequest(@NotNull EstadoPago estado) {
    }

    public record Response(
            Long id,
            Long idCliente,
            String cliente,
            Long idArrendatario,
            String arrendatario,
            Long idReservacion,
            EstadoPago estadoPago,
            LocalDate fechaCreacion,
            LocalDate fechaConfirmacion,
            MetodoPago metodo,
            BigDecimal montoTotal) {

        public static Response from(PagoModel p) {
            return new Response(
                    p.getId(),
                    p.getCliente().getId(),
                    p.getCliente().getNombreCompleto(),
                    p.getArrendatario().getId(),
                    p.getArrendatario().getNombreCompleto(),
                    p.getReservacion().getId(),
                    p.getEstadoPago(),
                    p.getFechaCreacion(),
                    p.getFechaConfirmacion(),
                    p.getMetodo(),
                    p.getMontoTotal());
        }
    }

    /**
     * Estado de cuenta de una persona.
     * saldo = totalReservaciones - totalConfirmado
     */
    public record EstadoCuenta(
            Long idCliente,
            String nombre,
            String rol,
            BigDecimal totalReservaciones,
            BigDecimal totalConfirmado,
            BigDecimal totalPorConfirmar,   // pagos RECIBIDO
            BigDecimal totalPendiente,      // pagos PENDIENTE + NO_RECIBIDO
            BigDecimal saldo,
            List<Response> pagos) {
    }
}
