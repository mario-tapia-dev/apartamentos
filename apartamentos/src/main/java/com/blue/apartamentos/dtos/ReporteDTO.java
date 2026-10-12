package com.blue.apartamentos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.blue.apartamentos.enums.TipoPropiedad;

public final class ReporteDTO {

    private ReporteDTO() {
    }

    // ---------- Ocupación ----------
    public record OcupacionPropiedad(
            Long idPropiedad,
            String titulo,
            TipoPropiedad tipo,
            long nochesOcupadas,
            long nochesPeriodo,
            BigDecimal porcentajeOcupacion) {
    }

    /** El periodo es [desde, hasta): la fecha "hasta" no cuenta como noche. */
    public record Ocupacion(
            LocalDate desde,
            LocalDate hasta,
            int totalPropiedades,
            long nochesOcupadas,
            long nochesDisponiblesTotales,
            BigDecimal ocupacionGlobal,
            List<OcupacionPropiedad> propiedades) {
    }

    // ---------- Ingresos ----------
    public record IngresoPropietario(
            Long idPropietario,
            String propietario,
            long numeroPagos,
            BigDecimal total) {
    }

    /** Suma de pagos CONFIRMADOS cuya fecha_confirmacion cae entre desde y hasta (ambas inclusive). */
    public record Ingresos(
            LocalDate desde,
            LocalDate hasta,
            long numeroPagos,
            BigDecimal totalIngresos,
            Map<String, BigDecimal> porMetodo,
            List<IngresoPropietario> porPropietario) {
    }

    // ---------- Morosidad ----------
    public record Deudor(
            Long idCliente,
            String nombre,
            String email,
            String telefono,
            long pagosVencidos,
            BigDecimal montoAdeudado,
            long diasMaximoAtraso) {
    }

    public record Morosidad(
            LocalDate fechaCorte,
            int diasGracia,
            BigDecimal totalAdeudado,
            List<Deudor> deudores) {
    }
}
