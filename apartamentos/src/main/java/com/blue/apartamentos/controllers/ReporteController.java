package com.blue.apartamentos.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blue.apartamentos.dtos.ReporteDTO;
import com.blue.apartamentos.services.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    // GET /api/reportes/ocupacion?desde=2026-09-01&hasta=2026-10-01   (hasta NO cuenta como noche)
    @GetMapping("/ocupacion")
    public ReporteDTO.Ocupacion ocupacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ocupacion(desde, hasta);
    }

    // GET /api/reportes/ingresos?desde=2026-09-01&hasta=2026-09-30   (ambas inclusive)
    @GetMapping("/ingresos")
    public ReporteDTO.Ingresos ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ingresos(desde, hasta);
    }

    // GET /api/reportes/morosidad
    @GetMapping("/morosidad")
    public ReporteDTO.Morosidad morosidad() {
        return service.morosidad();
    }
}
