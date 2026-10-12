package com.blue.apartamentos.dtos;

import com.blue.apartamentos.enums.EstadoHab;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class DetallePropiedadDTO {

    private DetallePropiedadDTO() {
    }

    public record Request(@NotNull @Min(1) Integer numeroHabitacion, EstadoHab estadoHab) {
    }

    public record EstadoRequest(@NotNull EstadoHab estadoHab) {
    }
}
