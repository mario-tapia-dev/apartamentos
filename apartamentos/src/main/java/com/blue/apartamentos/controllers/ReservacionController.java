package com.blue.apartamentos.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.blue.apartamentos.dtos.ReservacionDTO;
import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.enums.TipoPropiedad;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.services.ReservacionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservaciones")
public class ReservacionController {

    private final ReservacionService service;

    public ReservacionController(ReservacionService service) {
        this.service = service;
    }

    // ---- consultas ----

    // /api/reservaciones  o  /api/reservaciones?estado=CONFIRMADA
    @GetMapping
    public List<ReservacionDTO.Response> listar(@RequestParam(required = false) EstadoReservacion estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public ReservacionDTO.Response obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/cliente/{idCliente}")
    public List<ReservacionDTO.Response> porCliente(@PathVariable Long idCliente) {
        return service.porCliente(idCliente);
    }

    @GetMapping("/arrendatario/{idArrendatario}")
    public List<ReservacionDTO.Response> porArrendatario(@PathVariable Long idArrendatario) {
        return service.porArrendatario(idArrendatario);
    }

    @GetMapping("/propiedad/{idPropiedad}")
    public List<ReservacionDTO.Response> porPropiedad(@PathVariable Long idPropiedad) {
        return service.porPropiedad(idPropiedad);
    }

    // ---- disponibilidad ----

    // ¿Está libre esta propiedad entre estas fechas?
    // GET /api/reservaciones/propiedad/3/disponible?checkin=2026-10-01&checkout=2026-10-05
    @GetMapping("/propiedad/{idPropiedad}/disponible")
    public Map<String, Object> propiedadDisponible(@PathVariable Long idPropiedad,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        boolean disponible = service.propiedadDisponible(idPropiedad, checkin, checkout);
        return Map.of("idPropiedad", idPropiedad, "checkin", checkin, "checkout", checkout,
                "disponible", disponible);
    }

    // Propiedades libres en un rango (opcional: &tipo=CASA)
    // GET /api/reservaciones/disponibles?checkin=2026-10-01&checkout=2026-10-05
    @GetMapping("/disponibles")
    public List<PropiedadModel> propiedadesDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
            @RequestParam(required = false) TipoPropiedad tipo) {
        return service.propiedadesDisponibles(checkin, checkout, tipo);
    }

    // ---- altas y cambios ----

    @PostMapping
    public ResponseEntity<ReservacionDTO.Response> crear(@Valid @RequestBody ReservacionDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}/fechas")
    public ReservacionDTO.Response cambiarFechas(@PathVariable Long id,
            @Valid @RequestBody ReservacionDTO.FechasRequest req) {
        return service.cambiarFechas(id, req);
    }

    // ---- flujo de estados ----

    @PatchMapping("/{id}/confirmar")
    public ReservacionDTO.Response confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PatchMapping("/{id}/checkin")
    public ReservacionDTO.Response checkIn(@PathVariable Long id) {
        return service.checkIn(id);
    }

    @PatchMapping("/{id}/checkout")
    public ReservacionDTO.Response checkOut(@PathVariable Long id) {
        return service.checkOut(id);
    }

    @PatchMapping("/{id}/cancelar")
    public ReservacionDTO.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
