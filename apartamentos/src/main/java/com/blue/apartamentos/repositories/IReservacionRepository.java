package com.blue.apartamentos.repositories;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.models.ReservacionModel;

@Repository
public interface IReservacionRepository extends JpaRepository<ReservacionModel, Long> {

    @Query("SELECT r FROM ReservacionModel r WHERE r.cliente.id = :idCliente ORDER BY r.fechaCheckin DESC")
    List<ReservacionModel> buscarPorCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT r FROM ReservacionModel r WHERE r.arrendatario.id = :idArrendatario ORDER BY r.fechaCheckin DESC")
    List<ReservacionModel> buscarPorArrendatario(@Param("idArrendatario") Long idArrendatario);

    @Query("SELECT r FROM ReservacionModel r WHERE r.propiedad.id_propiedad = :idPropiedad ORDER BY r.fechaCheckin DESC")
    List<ReservacionModel> buscarPorPropiedad(@Param("idPropiedad") Long idPropiedad);

    @Query("SELECT r FROM ReservacionModel r WHERE r.estado = :estado ORDER BY r.fechaCheckin DESC")
    List<ReservacionModel> buscarPorEstado(@Param("estado") EstadoReservacion estado);

    /**
     * Cuenta reservaciones de la propiedad que se traslapan con [checkin, checkout).
     * Dos rangos se traslapan si inicio1 < fin2 Y fin1 > inicio2.
     * excluirId sirve al editar fechas (para no chocar con la misma reservación); usar -1 si no aplica.
     */
    @Query("""
            SELECT COUNT(r) FROM ReservacionModel r
            WHERE r.propiedad.id_propiedad = :idPropiedad
              AND r.estado IN :estados
              AND r.fechaCheckin < :checkout
              AND r.fechaCheckout > :checkin
              AND r.id <> :excluirId
            """)
    long contarTraslapes(
            @Param("idPropiedad") Long idPropiedad,
            @Param("estados") Collection<EstadoReservacion> estados,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout,
            @Param("excluirId") Long excluirId);

    /** Reservaciones que tocan el periodo [desde, hasta) (para el reporte de ocupación). */
    @Query("""
            SELECT r FROM ReservacionModel r JOIN FETCH r.propiedad
            WHERE r.estado IN :estados
              AND r.fechaCheckin < :hasta
              AND r.fechaCheckout > :desde
            """)
    List<ReservacionModel> buscarEnPeriodo(
            @Param("estados") Collection<EstadoReservacion> estados,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
