package com.blue.apartamentos.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blue.apartamentos.enums.TipoPropiedad;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.services.PropiedadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/propiedades")
public class PropiedadController {

    @Autowired
    private PropiedadService propiedadService;

    // Recuperar todas las propiedades
    @GetMapping
    public List<PropiedadModel> getAllPropiedades() {
        return propiedadService.getAllPropiedades();
    }

    // Propiedad por ID
    @GetMapping("/{id}")
    public ResponseEntity<PropiedadModel> getPropiedadesById(@PathVariable Long id) {
        Optional<PropiedadModel> propiedad = propiedadService.getPropiedadById(id);
        return propiedad.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear nueva propiedad. El propietario se manda como: "propietario": { "id": 1 }
    @PostMapping
    public ResponseEntity<PropiedadModel> createPropiedad(@Valid @RequestBody PropiedadModel propiedad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propiedadService.savePropiedad(propiedad));
    }

    // Actualizar propiedad existente
    @PutMapping("/{id}")
    public PropiedadModel updatePropiedad(@PathVariable Long id, @Valid @RequestBody PropiedadModel propiedadDetails) {
        return propiedadService.updatePropiedad(id, propiedadDetails);
    }

    // Eliminar una propiedad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePropiedad(@PathVariable Long id) {
        propiedadService.deletePropiedad(id);
        return ResponseEntity.noContent().build();
    }

    // ACTIVIDAD: Recuperar propiedades por tipo
    @GetMapping("/tipo/{tipo}")
    public List<PropiedadModel> getPropiedadesByTipo(@PathVariable TipoPropiedad tipo) {
        return propiedadService.getPropiedadesByTipo(tipo);
    }

    // ACTIVIDAD: Recuperar todas las propiedades de un Propietario
    @GetMapping("/propietario/{idPropietario}")
    public List<PropiedadModel> getPropiedadesByPropietario(@PathVariable Long idPropietario) {
        return propiedadService.getPropiedadesByPropietario(idPropietario);
    }
}
