package com.blue.apartamentos.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.dtos.ReporteDTO;
import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.exceptions.ReglaNegocioException;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.models.PagoModel;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.models.ReservacionModel;
import com.blue.apartamentos.repositories.IPagoRepository;
import com.blue.apartamentos.repositories.IPropiedadRepository;
import com.blue.apartamentos.repositories.IReservacionRepository;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    /** Para ocupación solo cuentan reservaciones ya confirmadas, en curso o terminadas. */
    private static final List<EstadoReservacion> ESTADOS_OCUPACION = List.of(
            EstadoReservacion.CONFIRMADA, EstadoReservacion.ACTIVA, EstadoReservacion.FINALIZADA);

    private final IPropiedadRepository propiedadRepository;
    private final IReservacionRepository reservacionRepository;
    private final IPagoRepository pagoRepository;

    /** Días que se le dan al cliente antes de considerar moroso un pago sin cobrar. */
    @Value("${app.pagos.dias-gracia:3}")
    private int diasGracia;

    public ReporteService(IPropiedadRepository propiedadRepository, IReservacionRepository reservacionRepository,
            IPagoRepository pagoRepository) {
        this.propiedadRepository = propiedadRepository;
        this.reservacionRepository = reservacionRepository;
        this.pagoRepository = pagoRepository;
    }

    // ------------------------------------------------------------------ ocupación

    /** Periodo [desde, hasta). Si no se mandan fechas se usa el mes actual. */
    public ReporteDTO.Ocupacion ocupacion(LocalDate desde, LocalDate hasta) {
        if (desde == null) {
            desde = LocalDate.now().withDayOfMonth(1);
        }
        if (hasta == null) {
            hasta = desde.plusMonths(1);
        }
        if (!hasta.isAfter(desde)) {
            throw new ReglaNegocioException("\"hasta\" debe ser posterior a \"desde\"");
        }
        long nochesPeriodo = ChronoUnit.DAYS.between(desde, hasta);

        List<PropiedadModel> propiedades = propiedadRepository.findAll();
        Map<Long, Long> nochesPorPropiedad = new HashMap<>();
        for (ReservacionModel r : reservacionRepository.buscarEnPeriodo(ESTADOS_OCUPACION, desde, hasta)) {
            LocalDate ini = r.getFechaCheckin().isAfter(desde) ? r.getFechaCheckin() : desde;
            LocalDate fin = r.getFechaCheckout().isBefore(hasta) ? r.getFechaCheckout() : hasta;
            long noches = Math.max(0, ChronoUnit.DAYS.between(ini, fin));
            nochesPorPropiedad.merge(r.getPropiedad().getId_propiedad(), noches, Long::sum);
        }

        List<ReporteDTO.OcupacionPropiedad> detalle = new ArrayList<>();
        long totalOcupadas = 0;
        for (PropiedadModel p : propiedades) {
            long ocupadas = nochesPorPropiedad.getOrDefault(p.getId_propiedad(), 0L);
            totalOcupadas += ocupadas;
            detalle.add(new ReporteDTO.OcupacionPropiedad(p.getId_propiedad(), p.getTitulo(), p.getTipo(),
                    ocupadas, nochesPeriodo, porcentaje(ocupadas, nochesPeriodo)));
        }
        detalle.sort(Comparator.comparing(ReporteDTO.OcupacionPropiedad::porcentajeOcupacion).reversed());

        long disponiblesTotales = nochesPeriodo * propiedades.size();
        return new ReporteDTO.Ocupacion(desde, hasta, propiedades.size(), totalOcupadas, disponiblesTotales,
                porcentaje(totalOcupadas, disponiblesTotales), detalle);
    }

    // ------------------------------------------------------------------ ingresos

    /** Pagos CONFIRMADOS entre desde y hasta (inclusive). Si no se mandan fechas: del día 1 del mes a hoy. */
    public ReporteDTO.Ingresos ingresos(LocalDate desde, LocalDate hasta) {
        if (desde == null) {
            desde = LocalDate.now().withDayOfMonth(1);
        }
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("\"hasta\" no puede ser anterior a \"desde\"");
        }
        List<PagoModel> pagos = pagoRepository.buscarConfirmadosEntre(EstadoPago.CONFIRMADO, desde, hasta);

        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> porMetodo = new TreeMap<>();
        Map<Long, ReporteDTO.IngresoPropietario> porPropietario = new LinkedHashMap<>();

        for (PagoModel p : pagos) {
            total = total.add(p.getMontoTotal());
            porMetodo.merge(p.getMetodo().name(), p.getMontoTotal(), BigDecimal::add);

            ClienteModel dueno = p.getArrendatario();
            ReporteDTO.IngresoPropietario previo = porPropietario.get(dueno.getId());
            porPropietario.put(dueno.getId(), new ReporteDTO.IngresoPropietario(
                    dueno.getId(),
                    dueno.getNombreCompleto(),
                    (previo == null ? 0 : previo.numeroPagos()) + 1,
                    (previo == null ? BigDecimal.ZERO : previo.total()).add(p.getMontoTotal())));
        }

        List<ReporteDTO.IngresoPropietario> lista = new ArrayList<>(porPropietario.values());
        lista.sort(Comparator.comparing(ReporteDTO.IngresoPropietario::total).reversed());
        return new ReporteDTO.Ingresos(desde, hasta, pagos.size(), total, porMetodo, lista);
    }

    // ------------------------------------------------------------------ morosidad

    /**
     * Deudores: clientes con pagos PENDIENTE o NO_RECIBIDO creados hace más de "dias-gracia" días.
     */
    public ReporteDTO.Morosidad morosidad() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.minusDays(diasGracia);
        List<PagoModel> vencidos = pagoRepository.buscarVencidos(
                EnumSet.of(EstadoPago.PENDIENTE, EstadoPago.NO_RECIBIDO), limite);

        Map<Long, List<PagoModel>> porCliente = new LinkedHashMap<>();
        for (PagoModel p : vencidos) {
            porCliente.computeIfAbsent(p.getCliente().getId(), k -> new ArrayList<>()).add(p);
        }

        List<ReporteDTO.Deudor> deudores = new ArrayList<>();
        BigDecimal totalAdeudado = BigDecimal.ZERO;
        for (List<PagoModel> pagos : porCliente.values()) {
            ClienteModel c = pagos.get(0).getCliente();
            BigDecimal monto = pagos.stream().map(PagoModel::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate masAntiguo = pagos.stream().map(PagoModel::getFechaCreacion).min(LocalDate::compareTo).get();
            long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(masAntiguo.plusDays(diasGracia), hoy));

            totalAdeudado = totalAdeudado.add(monto);
            deudores.add(new ReporteDTO.Deudor(c.getId(), c.getNombreCompleto(), c.getEmail(), c.getTelefono(),
                    pagos.size(), monto, diasAtraso));
        }
        deudores.sort(Comparator.comparing(ReporteDTO.Deudor::montoAdeudado).reversed());
        return new ReporteDTO.Morosidad(hoy, diasGracia, totalAdeudado, deudores);
    }

    private BigDecimal porcentaje(long parte, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
