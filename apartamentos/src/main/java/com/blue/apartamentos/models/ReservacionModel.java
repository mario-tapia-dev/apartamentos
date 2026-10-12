package com.blue.apartamentos.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.blue.apartamentos.enums.EstadoReservacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservaciones")
public class ReservacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_propiedad", nullable = false)
    private PropiedadModel propiedad;

    // Quien renta (paga)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteModel cliente;

    // Quien ofrece la propiedad y recibe el pago (el propietario)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_arrendatario", nullable = false)
    private ClienteModel arrendatario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_checkin", nullable = false)
    private LocalDate fechaCheckin;

    // Fecha programada de salida
    @Column(name = "fecha_checkout", nullable = false)
    private LocalDate fechaCheckout;

    // Fecha real en la que el cliente salió (se llena al hacer check-out)
    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reservacion", nullable = false, length = 20)
    private EstadoReservacion estado = EstadoReservacion.PENDIENTE;

    // noches x precio_noche al momento de reservar
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @PrePersist
    public void onPrePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PropiedadModel getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(PropiedadModel propiedad) {
        this.propiedad = propiedad;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public ClienteModel getArrendatario() {
        return arrendatario;
    }

    public void setArrendatario(ClienteModel arrendatario) {
        this.arrendatario = arrendatario;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaCheckin() {
        return fechaCheckin;
    }

    public void setFechaCheckin(LocalDate fechaCheckin) {
        this.fechaCheckin = fechaCheckin;
    }

    public LocalDate getFechaCheckout() {
        return fechaCheckout;
    }

    public void setFechaCheckout(LocalDate fechaCheckout) {
        this.fechaCheckout = fechaCheckout;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public EstadoReservacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoReservacion estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}
