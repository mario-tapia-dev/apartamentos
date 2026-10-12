package com.blue.apartamentos.repositories;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blue.apartamentos.enums.EstadoHab;
import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.enums.TipoPropiedad;
import com.blue.apartamentos.models.PropiedadModel;

@Repository
public interface IPropiedadRepository extends JpaRepository<PropiedadModel, Long> {

    @Query("SELECT p FROM PropiedadModel p WHERE p.tipo = :tipo")
    List<PropiedadModel> buscarPorTipo(@Param("tipo") TipoPropiedad tipo);

    @Query("SELECT p FROM PropiedadModel p WHERE p.propietario.id = :idPropietario")
    List<PropiedadModel> buscarPorPropietario(@Param("idPropietario") Long idPropietario);

    /** Propiedades en estado DISPONIBLE sin reservaciones que se traslapen con [checkin, checkout). */
    @Query("""
            SELECT p FROM PropiedadModel p
            WHERE p.estado = :estadoPropiedad
              AND NOT EXISTS (
                  SELECT r.id FROM ReservacionModel r
                  WHERE r.propiedad = p
                    AND r.estado IN :estadosQueBloquean
                    AND r.fechaCheckin < :checkout
                    AND r.fechaCheckout > :checkin)
            """)
    List<PropiedadModel> buscarDisponibles(
            @Param("estadoPropiedad") EstadoHab estadoPropiedad,
            @Param("estadosQueBloquean") Collection<EstadoReservacion> estadosQueBloquean,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout);
}
