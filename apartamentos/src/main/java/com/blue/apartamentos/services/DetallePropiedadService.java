package com.blue.apartamentos.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.enums.EstadoHab;
import com.blue.apartamentos.exceptions.RecursoNoEncontradoException;
import com.blue.apartamentos.exceptions.ReglaNegocioException;
import com.blue.apartamentos.models.DetallePropiedadId;
import com.blue.apartamentos.models.DetallePropiedadModel;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.repositories.IDetallePropiedadRepository;
import com.blue.apartamentos.repositories.IPropiedadRepository;

@Service
public class DetallePropiedadService {

    private final IDetallePropiedadRepository detalleRepository;
    private final IPropiedadRepository propiedadRepository;

    public DetallePropiedadService(IDetallePropiedadRepository detalleRepository,
            IPropiedadRepository propiedadRepository) {
        this.detalleRepository = detalleRepository;
        this.propiedadRepository = propiedadRepository;
    }

    public List<DetallePropiedadModel> listar(Long idPropiedad) {
        obtenerPropiedad(idPropiedad);
        return detalleRepository.buscarPorPropiedad(idPropiedad);
    }

    public DetallePropiedadModel obtener(Long idPropiedad, Integer numeroHabitacion) {
        return detalleRepository.findById(new DetallePropiedadId(idPropiedad, numeroHabitacion))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Habitación " + numeroHabitacion + " de la propiedad " + idPropiedad + " no encontrada"));
    }

    @Transactional
    public DetallePropiedadModel crear(Long idPropiedad, Integer numeroHabitacion, EstadoHab estado) {
        PropiedadModel propiedad = obtenerPropiedad(idPropiedad);

        if (numeroHabitacion > propiedad.getNum_habitaciones()) {
            throw new ReglaNegocioException("La propiedad solo tiene " + propiedad.getNum_habitaciones()
                    + " habitación(es); el número " + numeroHabitacion + " no es válido");
        }
        DetallePropiedadId id = new DetallePropiedadId(idPropiedad, numeroHabitacion);
        if (detalleRepository.existsById(id)) {
            throw new ReglaNegocioException(
                    "La habitación " + numeroHabitacion + " ya está registrada en la propiedad " + idPropiedad,
                    HttpStatus.CONFLICT);
        }
        return detalleRepository.save(new DetallePropiedadModel(propiedad, numeroHabitacion, estado));
    }

    @Transactional
    public DetallePropiedadModel cambiarEstado(Long idPropiedad, Integer numeroHabitacion, EstadoHab estado) {
        DetallePropiedadModel d = obtener(idPropiedad, numeroHabitacion);
        d.setEstadoHab(estado);
        return detalleRepository.save(d);
    }

    @Transactional
    public void eliminar(Long idPropiedad, Integer numeroHabitacion) {
        detalleRepository.delete(obtener(idPropiedad, numeroHabitacion));
    }

    private PropiedadModel obtenerPropiedad(Long idPropiedad) {
        return propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Propiedad", idPropiedad));
    }
}
