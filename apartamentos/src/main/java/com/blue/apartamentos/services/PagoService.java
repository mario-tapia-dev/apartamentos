package com.blue.apartamentos.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.dtos.PagoDTO;
import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.enums.MetodoPago;
import com.blue.apartamentos.exceptions.RecursoNoEncontradoException;
import com.blue.apartamentos.exceptions.ReglaNegocioException;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.models.PagoModel;
import com.blue.apartamentos.models.ReservacionModel;
import com.blue.apartamentos.repositories.IClienteRepository;
import com.blue.apartamentos.repositories.IPagoRepository;
import com.blue.apartamentos.repositories.IReservacionRepository;

@Service
public class PagoService {

    private static final Set<EstadoPago> SIN_COBRAR = Set.of(EstadoPago.PENDIENTE, EstadoPago.NO_RECIBIDO);

    private final IPagoRepository pagoRepository;
    private final IReservacionRepository reservacionRepository;
    private final IClienteRepository clienteRepository;

    public PagoService(IPagoRepository pagoRepository, IReservacionRepository reservacionRepository,
            IClienteRepository clienteRepository) {
        this.pagoRepository = pagoRepository;
        this.reservacionRepository = reservacionRepository;
        this.clienteRepository = clienteRepository;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> listar(EstadoPago estado) {
        List<PagoModel> lista = (estado == null) ? pagoRepository.findAll() : pagoRepository.buscarPorEstado(estado);
        return lista.stream().map(PagoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public PagoDTO.Response obtener(Long id) {
        return PagoDTO.Response.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> porCliente(Long idCliente) {
        buscarCliente(idCliente);
        return pagoRepository.buscarPorCliente(idCliente).stream().map(PagoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> porArrendatario(Long idArrendatario) {
        buscarCliente(idArrendatario);
        return pagoRepository.buscarPorArrendatario(idArrendatario).stream().map(PagoDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PagoDTO.Response> porReservacion(Long idReservacion) {
        buscarReservacion(idReservacion);
        return pagoRepository.buscarPorReservacion(idReservacion).stream().map(PagoDTO.Response::from).toList();
    }

    // ------------------------------------------------------------------ altas y cambios

    @Transactional
    public PagoDTO.Response crear(PagoDTO.Request req) {
        ReservacionModel reservacion = buscarReservacion(req.idReservacion());
        if (reservacion.getEstado() == EstadoReservacion.CANCELADA) {
            throw new ReglaNegocioException("No se pueden registrar pagos de una reservación CANCELADA");
        }

        // Evita cobrar de más / pagos duplicados: lo ya registrado (sin contar cancelados) se descuenta
        BigDecimal yaRegistrado = pagoRepository.sumarPorReservacionExcepto(reservacion.getId(), EstadoPago.CANCELADO);
        if (yaRegistrado == null) {
            yaRegistrado = BigDecimal.ZERO;
        }
        BigDecimal saldo = reservacion.getMontoTotal().subtract(yaRegistrado);
        if (saldo.signum() <= 0) {
            throw new ReglaNegocioException("La reservación ya tiene pagos por el total (" + reservacion.getMontoTotal() + ")");
        }
        BigDecimal monto = (req.montoTotal() != null) ? req.montoTotal() : saldo;
        if (monto.compareTo(saldo) > 0) {
            throw new ReglaNegocioException("El monto (" + monto + ") excede el saldo por pagar (" + saldo + ")");
        }

        ClienteModel arrendatario = reservacion.getArrendatario();
        if (req.metodo() == MetodoPago.TRANSFERENCIA
                && (arrendatario.getClabe() == null || arrendatario.getClabe().isBlank())) {
            throw new ReglaNegocioException("El propietario (id " + arrendatario.getId()
                    + ") no tiene CLABE registrada, no se puede pagar por transferencia");
        }

        PagoModel p = new PagoModel();
        p.setReservacion(reservacion);
        p.setCliente(reservacion.getCliente());
        p.setArrendatario(arrendatario);
        p.setMetodo(req.metodo());
        p.setMontoTotal(monto);
        p.setEstadoPago(EstadoPago.PENDIENTE);
        return PagoDTO.Response.from(pagoRepository.saveAndFlush(p));
    }

    /**
     * Mueve el pago por su flujo: PENDIENTE -> RECIBIDO / NO_RECIBIDO -> CONFIRMADO (o CANCELADO).
     * Al confirmar se guarda fecha_confirmacion y, si la reservación estaba PENDIENTE, pasa a CONFIRMADA.
     */
    @Transactional
    public PagoDTO.Response cambiarEstado(Long id, EstadoPago nuevo) {
        PagoModel p = buscar(id);
        if (!p.getEstadoPago().puedePasarA(nuevo)) {
            throw new ReglaNegocioException("Transición no permitida: " + p.getEstadoPago() + " -> " + nuevo);
        }
        p.setEstadoPago(nuevo);

        if (nuevo == EstadoPago.CONFIRMADO) {
            p.setFechaConfirmacion(LocalDate.now());
            ReservacionModel r = p.getReservacion();
            if (r.getEstado() == EstadoReservacion.PENDIENTE) {
                r.setEstado(EstadoReservacion.CONFIRMADA);
                reservacionRepository.save(r);
            }
        }
        return PagoDTO.Response.from(pagoRepository.saveAndFlush(p));
    }

    @Transactional
    public void eliminar(Long id) {
        PagoModel p = buscar(id);
        if (p.getEstadoPago() != EstadoPago.PENDIENTE && p.getEstadoPago() != EstadoPago.CANCELADO) {
            throw new ReglaNegocioException("Solo se pueden eliminar pagos PENDIENTE o CANCELADO; "
                    + "el resto debe conservarse como historial");
        }
        pagoRepository.delete(p);
    }

    // ------------------------------------------------------------------ estados de cuenta

    /** Estado de cuenta de quien PAGA rentas. */
    @Transactional(readOnly = true)
    public PagoDTO.EstadoCuenta estadoCuentaCliente(Long idCliente) {
        ClienteModel c = buscarCliente(idCliente);
        return armarEstadoCuenta(c, "CLIENTE",
                reservacionRepository.buscarPorCliente(idCliente),
                pagoRepository.buscarPorCliente(idCliente));
    }

    /** Estado de cuenta de quien RECIBE los pagos (propietario). */
    @Transactional(readOnly = true)
    public PagoDTO.EstadoCuenta estadoCuentaArrendatario(Long idArrendatario) {
        ClienteModel c = buscarCliente(idArrendatario);
        return armarEstadoCuenta(c, "ARRENDATARIO",
                reservacionRepository.buscarPorArrendatario(idArrendatario),
                pagoRepository.buscarPorArrendatario(idArrendatario));
    }

    private PagoDTO.EstadoCuenta armarEstadoCuenta(ClienteModel c, String rol,
            List<ReservacionModel> reservaciones, List<PagoModel> pagos) {
        BigDecimal totalReservaciones = reservaciones.stream()
                .filter(r -> r.getEstado() != EstadoReservacion.CANCELADA)
                .map(ReservacionModel::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal confirmado = sumar(pagos, Set.of(EstadoPago.CONFIRMADO));
        BigDecimal porConfirmar = sumar(pagos, Set.of(EstadoPago.RECIBIDO));
        BigDecimal pendiente = sumar(pagos, SIN_COBRAR);

        return new PagoDTO.EstadoCuenta(
                c.getId(), c.getNombreCompleto(), rol,
                totalReservaciones, confirmado, porConfirmar, pendiente,
                totalReservaciones.subtract(confirmado),
                pagos.stream().map(PagoDTO.Response::from).toList());
    }

    private BigDecimal sumar(List<PagoModel> pagos, Set<EstadoPago> estados) {
        return pagos.stream()
                .filter(p -> estados.contains(p.getEstadoPago()))
                .map(PagoModel::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------------------------------------------------------ utilidades

    private PagoModel buscar(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    private ClienteModel buscarCliente(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private ReservacionModel buscarReservacion(Long id) {
        return reservacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reservación", id));
    }
}
