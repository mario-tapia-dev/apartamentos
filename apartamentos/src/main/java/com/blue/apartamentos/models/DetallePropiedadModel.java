package com.blue.apartamentos.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.blue.apartamentos.enums.EstadoHab;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/** Cada habitación de una propiedad. PK = (id_propiedad, numero_habitacion). */
@Entity
@Table(name = "detalle_propiedad")
public class DetallePropiedadModel {

    @EmbeddedId
    private DetallePropiedadId id;

    @MapsId("idPropiedad")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private PropiedadModel propiedad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_hab", nullable = false, length = 20)
    private EstadoHab estadoHab = EstadoHab.DISPONIBLE;

    public DetallePropiedadModel() {
    }

    public DetallePropiedadModel(PropiedadModel propiedad, Integer numeroHabitacion, EstadoHab estadoHab) {
        this.id = new DetallePropiedadId(propiedad.getId_propiedad(), numeroHabitacion);
        this.propiedad = propiedad;
        if (estadoHab != null) {
            this.estadoHab = estadoHab;
        }
    }

    @JsonIgnore
    public DetallePropiedadId getId() {
        return id;
    }

    public void setId(DetallePropiedadId id) {
        this.id = id;
    }

    @JsonIgnore
    public PropiedadModel getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(PropiedadModel propiedad) {
        this.propiedad = propiedad;
    }

    public Long getIdPropiedad() {
        return id.getIdPropiedad();
    }

    public Integer getNumeroHabitacion() {
        return id.getNumeroHabitacion();
    }

    public EstadoHab getEstadoHab() {
        return estadoHab;
    }

    public void setEstadoHab(EstadoHab estadoHab) {
        this.estadoHab = estadoHab;
    }
}
