package com.blue.apartamentos.models;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Llave primaria compuesta de detalle_propiedad: (id_propiedad, numero_habitacion). */
@Embeddable
public class DetallePropiedadId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_propiedad")
    private Long idPropiedad;

    @Column(name = "numero_habitacion")
    private Integer numeroHabitacion;

    public DetallePropiedadId() {
    }

    public DetallePropiedadId(Long idPropiedad, Integer numeroHabitacion) {
        this.idPropiedad = idPropiedad;
        this.numeroHabitacion = numeroHabitacion;
    }

    public Long getIdPropiedad() {
        return idPropiedad;
    }

    public void setIdPropiedad(Long idPropiedad) {
        this.idPropiedad = idPropiedad;
    }

    public Integer getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(Integer numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePropiedadId other)) return false;
        return Objects.equals(idPropiedad, other.idPropiedad)
                && Objects.equals(numeroHabitacion, other.numeroHabitacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPropiedad, numeroHabitacion);
    }
}
