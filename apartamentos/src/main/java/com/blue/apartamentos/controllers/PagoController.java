package com.blue.apartamentos.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.blue.apartamentos.dtos.PagoDTO;
import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.services.PagoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    // /api/pagos  o  /api/pagos?estado=PENDIENTE
    @GetMapping
    public List<PagoDTO.Response> listar(@RequestParam(required = false) EstadoPago estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public PagoDTO.Response obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/cliente/{idCliente}")
    public List<PagoDTO.Response> porCliente(@PathVariable Long idCliente) {
        return service.porCliente(idCliente);
    }

    @GetMapping("/arrendatario/{idArrendatario}")
    public List<PagoDTO.Response> porArrendatario(@PathVariable Long idArrendatario) {
        return service.porArrendatario(idArrendatario);
    }

    @GetMapping("/reservacion/{idReservacion}")
    public List<PagoDTO.Response> porReservacion(@PathVariable Long idReservacion) {
        return service.porReservacion(idReservacion);
    }

    // ---- estados de cuenta ----

    @GetMapping("/estado-cuenta/cliente/{idCliente}")
    public PagoDTO.EstadoCuenta estadoCuentaCliente(@PathVariable Long idCliente) {
        return service.estadoCuentaCliente(idCliente);
    }

    @GetMapping("/estado-cuenta/arrendatario/{idArrendatario}")
    public PagoDTO.EstadoCuenta estadoCuentaArrendatario(@PathVariable Long idArrendatario) {
        return service.estadoCuentaArrendatario(idArrendatario);
    }

    // ---- altas y cambios ----

    @PostMapping
    public ResponseEntity<PagoDTO.Response> crear(@Valid @RequestBody PagoDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    // PATCH /api/pagos/5/estado   body: { "estado": "RECIBIDO" }
    @PatchMapping("/{id}/estado")
    public PagoDTO.Response cambiarEstado(@PathVariable Long id, @Valid @RequestBody PagoDTO.EstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
