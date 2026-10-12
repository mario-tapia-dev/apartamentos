package com.blue.apartamentos.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.enums.MetodoPago;

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
@Table(name = "pagos")
public class PagoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    // Quien paga la renta
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteModel cliente;

    // Quien recibe el pago (propietario)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_arrendatario", nullable = false)
    private ClienteModel arrendatario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reservacion", nullable = false)
    private ReservacionModel reservacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_confirmacion")
    private LocalDate fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 20)
    private MetodoPago metodo;

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

    public ReservacionModel getReservacion() {
        return reservacion;
    }

    public void setReservacion(ReservacionModel reservacion) {
        this.reservacion = reservacion;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDate fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}
