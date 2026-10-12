package com.blue.apartamentos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blue.apartamentos.models.DetallePropiedadId;
import com.blue.apartamentos.models.DetallePropiedadModel;

@Repository
public interface IDetallePropiedadRepository extends JpaRepository<DetallePropiedadModel, DetallePropiedadId> {

    @Query("SELECT d FROM DetallePropiedadModel d WHERE d.id.idPropiedad = :idPropiedad ORDER BY d.id.numeroHabitacion")
    List<DetallePropiedadModel> buscarPorPropiedad(@Param("idPropiedad") Long idPropiedad);

    @Modifying
    @Query("DELETE FROM DetallePropiedadModel d WHERE d.id.idPropiedad = :idPropiedad")
    void eliminarPorPropiedad(@Param("idPropiedad") Long idPropiedad);
}
