package com.blue.apartamentos.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.models.PagoModel;

@Repository
public interface IPagoRepository extends JpaRepository<PagoModel, Long> {

    @Query("SELECT p FROM PagoModel p WHERE p.cliente.id = :idCliente ORDER BY p.fechaCreacion DESC")
    List<PagoModel> buscarPorCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT p FROM PagoModel p WHERE p.arrendatario.id = :idArrendatario ORDER BY p.fechaCreacion DESC")
    List<PagoModel> buscarPorArrendatario(@Param("idArrendatario") Long idArrendatario);

    @Query("SELECT p FROM PagoModel p WHERE p.reservacion.id = :idReservacion ORDER BY p.fechaCreacion")
    List<PagoModel> buscarPorReservacion(@Param("idReservacion") Long idReservacion);

    @Query("SELECT p FROM PagoModel p WHERE p.estadoPago = :estado ORDER BY p.fechaCreacion DESC")
    List<PagoModel> buscarPorEstado(@Param("estado") EstadoPago estado);

    /** Suma de los pagos de una reservación que no están cancelados. Devuelve null si no hay pagos. */
    @Query("""
            SELECT SUM(p.montoTotal) FROM PagoModel p
            WHERE p.reservacion.id = :idReservacion AND p.estadoPago <> :excluido
            """)
    BigDecimal sumarPorReservacionExcepto(
            @Param("idReservacion") Long idReservacion,
            @Param("excluido") EstadoPago excluido);

    /** Pagos confirmados con fecha de confirmación entre desde y hasta (inclusive). */
    @Query("""
            SELECT p FROM PagoModel p JOIN FETCH p.arrendatario
            WHERE p.estadoPago = :confirmado
              AND p.fechaConfirmacion BETWEEN :desde AND :hasta
            """)
    List<PagoModel> buscarConfirmadosEntre(
            @Param("confirmado") EstadoPago confirmado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /** Pagos sin cobrar (en los estados dados) creados en o antes de la fecha límite. */
    @Query("""
            SELECT p FROM PagoModel p JOIN FETCH p.cliente
            WHERE p.estadoPago IN :estados
              AND p.fechaCreacion <= :fechaLimite
            """)
    List<PagoModel> buscarVencidos(
            @Param("estados") Collection<EstadoPago> estados,
            @Param("fechaLimite") LocalDate fechaLimite);
}
