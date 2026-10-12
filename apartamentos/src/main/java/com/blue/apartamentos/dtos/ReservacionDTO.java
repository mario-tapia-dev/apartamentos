package com.blue.apartamentos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.models.ReservacionModel;

import jakarta.validation.constraints.NotNull;

public final class ReservacionDTO {

    private ReservacionDTO() {
    }

    /** idArrendatario es opcional: si no se manda se toma el propietario de la propiedad. */
    public record Request(
            @NotNull Long idPropiedad,
            @NotNull Long idCliente,
            Long idArrendatario,
            @NotNull LocalDate fechaCheckin,
            @NotNull LocalDate fechaCheckout) {
    }

    public record FechasRequest(
            @NotNull LocalDate fechaCheckin,
            @NotNull LocalDate fechaCheckout) {
    }

    public record Response(
            Long id,
            Long idPropiedad,
            String propiedad,
            Long idCliente,
            String cliente,
            Long idArrendatario,
            String arrendatario,
            LocalDate fechaCreacion,
            LocalDate fechaCheckin,
            LocalDate fechaCheckout,
            LocalDate fechaSalida,
            EstadoReservacion estado,
            BigDecimal montoTotal) {

        public static Response from(ReservacionModel r) {
            return new Response(
                    r.getId(),
                    r.getPropiedad().getId_propiedad(),
                    r.getPropiedad().getTitulo(),
                    r.getCliente().getId(),
                    r.getCliente().getNombreCompleto(),
                    r.getArrendatario().getId(),
                    r.getArrendatario().getNombreCompleto(),
                    r.getFechaCreacion(),
                    r.getFechaCheckin(),
                    r.getFechaCheckout(),
                    r.getFechaSalida(),
                    r.getEstado(),
                    r.getMontoTotal());
        }
    }
}
