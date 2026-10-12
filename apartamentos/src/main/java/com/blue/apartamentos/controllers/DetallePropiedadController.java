package com.blue.apartamentos.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.blue.apartamentos.dtos.DetallePropiedadDTO;
import com.blue.apartamentos.models.DetallePropiedadModel;
import com.blue.apartamentos.services.DetallePropiedadService;

import jakarta.validation.Valid;

/** Habitaciones de una propiedad (tabla detalle_propiedad). */
@RestController
@RequestMapping("/api/propiedades/{idPropiedad}/habitaciones")
public class DetallePropiedadController {

    private final DetallePropiedadService service;

    public DetallePropiedadController(DetallePropiedadService service) {
        this.service = service;
    }

    @GetMapping
    public List<DetallePropiedadModel> listar(@PathVariable Long idPropiedad) {
        return service.listar(idPropiedad);
    }

    @GetMapping("/{numeroHabitacion}")
    public DetallePropiedadModel obtener(@PathVariable Long idPropiedad, @PathVariable Integer numeroHabitacion) {
        return service.obtener(idPropiedad, numeroHabitacion);
    }

    @PostMapping
    public ResponseEntity<DetallePropiedadModel> crear(@PathVariable Long idPropiedad,
            @Valid @RequestBody DetallePropiedadDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(idPropiedad, req.numeroHabitacion(), req.estadoHab()));
    }

    // Cambiar el estado: DISPONIBLE / NO_DISPONIBLE / MANTENIMIENTO
    @PutMapping("/{numeroHabitacion}")
    public DetallePropiedadModel cambiarEstado(@PathVariable Long idPropiedad,
            @PathVariable Integer numeroHabitacion,
            @Valid @RequestBody DetallePropiedadDTO.EstadoRequest req) {
        return service.cambiarEstado(idPropiedad, numeroHabitacion, req.estadoHab());
    }

    @DeleteMapping("/{numeroHabitacion}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idPropiedad, @PathVariable Integer numeroHabitacion) {
        service.eliminar(idPropiedad, numeroHabitacion);
        return ResponseEntity.noContent().build();
    }
}
