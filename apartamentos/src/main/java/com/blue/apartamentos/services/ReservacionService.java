package com.blue.apartamentos.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.dtos.ReservacionDTO;
import com.blue.apartamentos.enums.ClienteStatus;
import com.blue.apartamentos.enums.EstadoHab;
import com.blue.apartamentos.enums.EstadoPago;
import com.blue.apartamentos.enums.EstadoReservacion;
import com.blue.apartamentos.enums.TipoPropiedad;
import com.blue.apartamentos.exceptions.RecursoNoEncontradoException;
import com.blue.apartamentos.exceptions.ReglaNegocioException;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.models.PagoModel;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.models.ReservacionModel;
import com.blue.apartamentos.repositories.IClienteRepository;
import com.blue.apartamentos.repositories.IPagoRepository;
import com.blue.apartamentos.repositories.IPropiedadRepository;
import com.blue.apartamentos.repositories.IReservacionRepository;

@Service
public class ReservacionService {

    /** Estados de una reservación que ocupan la propiedad (bloquean fechas para otras reservas). */
    public static final List<EstadoReservacion> ESTADOS_QUE_BLOQUEAN = List.of(
            EstadoReservacion.PENDIENTE, EstadoReservacion.CONFIRMADA, EstadoReservacion.ACTIVA);

    private final IReservacionRepository reservacionRepository;
    private final IPropiedadRepository propiedadRepository;
    private final IClienteRepository clienteRepository;
    private final IPagoRepository pagoRepository;

    public ReservacionService(IReservacionRepository reservacionRepository,
            IPropiedadRepository propiedadRepository,
            IClienteRepository clienteRepository,
            IPagoRepository pagoRepository) {
        this.reservacionRepository = reservacionRepository;
        this.propiedadRepository = propiedadRepository;
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<ReservacionDTO.Response> listar(EstadoReservacion estado) {
        List<ReservacionModel> lista = (estado == null)
                ? reservacionRepository.findAll()
                : reservacionRepository.buscarPorEstado(estado);
        return lista.stream().map(ReservacionDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ReservacionDTO.Response obtener(Long id) {
        return ReservacionDTO.Response.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ReservacionDTO.Response> porCliente(Long idCliente) {
        validarCliente(idCliente);
        return reservacionRepository.buscarPorCliente(idCliente).stream()
                .map(ReservacionDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservacionDTO.Response> porArrendatario(Long idArrendatario) {
        validarCliente(idArrendatario);
        return reservacionRepository.buscarPorArrendatario(idArrendatario).stream()
                .map(ReservacionDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservacionDTO.Response> porPropiedad(Long idPropiedad) {
        if (!propiedadRepository.existsById(idPropiedad)) {
            throw new RecursoNoEncontradoException("Propiedad", idPropiedad);
        }
        return reservacionRepository.buscarPorPropiedad(idPropiedad).stream()
                .map(ReservacionDTO.Response::from).toList();
    }

    // ------------------------------------------------------------------ disponibilidad

    /** ¿La propiedad está libre entre checkin (inclusive) y checkout (exclusive)? */
    @Transactional(readOnly = true)
    public boolean propiedadDisponible(Long idPropiedad, LocalDate checkin, LocalDate checkout) {
        validarRango(checkin, checkout);
        PropiedadModel p = propiedadRepository.findById(idPropiedad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Propiedad", idPropiedad));
        return p.getEstado() == EstadoHab.DISPONIBLE && !hayTraslape(idPropiedad, checkin, checkout, -1L);
    }

    /** Propiedades libres en el rango de fechas, opcionalmente filtradas por tipo. */
    @Transactional(readOnly = true)
    public List<PropiedadModel> propiedadesDisponibles(LocalDate checkin, LocalDate checkout, TipoPropiedad tipo) {
        validarRango(checkin, checkout);
        return propiedadRepository
                .buscarDisponibles(EstadoHab.DISPONIBLE, ESTADOS_QUE_BLOQUEAN, checkin, checkout).stream()
                .filter(p -> tipo == null || p.getTipo() == tipo)
                .toList();
    }

    // ------------------------------------------------------------------ altas y cambios

    @Transactional
    public ReservacionDTO.Response crear(ReservacionDTO.Request req) {
        validarRango(req.fechaCheckin(), req.fechaCheckout());
        if (req.fechaCheckin().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de check-in no puede estar en el pasado");
        }

        PropiedadModel propiedad = propiedadRepository.findById(req.idPropiedad())
                .orElseThrow(() -> new RecursoNoEncontradoException("Propiedad", req.idPropiedad()));
        if (propiedad.getEstado() != EstadoHab.DISPONIBLE) {
            throw new ReglaNegocioException(
                    "La propiedad no se puede reservar (estado: " + propiedad.getEstado() + ")");
        }

        // El arrendatario (quien recibe el pago) debe ser el propietario de la propiedad
        Long idDueno = propiedad.getPropietario().getId();
        Long idArrendatario = req.idArrendatario() != null ? req.idArrendatario() : idDueno;
        if (!idArrendatario.equals(idDueno)) {
            throw new ReglaNegocioException("El arrendatario debe ser el propietario de la propiedad (id " + idDueno + ")");
        }
        if (req.idCliente().equals(idArrendatario)) {
            throw new ReglaNegocioException("El cliente no puede reservar su propia propiedad");
        }
        ClienteModel cliente = clienteActivo(req.idCliente(), "Cliente");
        ClienteModel arrendatario = clienteActivo(idArrendatario, "Arrendatario");

        if (hayTraslape(propiedad.getId_propiedad(), req.fechaCheckin(), req.fechaCheckout(), -1L)) {
            throw new ReglaNegocioException("La propiedad ya está reservada en esas fechas", HttpStatus.CONFLICT);
        }

        ReservacionModel r = new ReservacionModel();
        r.setPropiedad(propiedad);
        r.setCliente(cliente);
        r.setArrendatario(arrendatario);
        r.setFechaCheckin(req.fechaCheckin());
        r.setFechaCheckout(req.fechaCheckout());
        r.setEstado(EstadoReservacion.PENDIENTE);
        r.setMontoTotal(calcularMonto(propiedad, req.fechaCheckin(), req.fechaCheckout()));
        return ReservacionDTO.Response.from(reservacionRepository.saveAndFlush(r));
    }

    @Transactional
    public ReservacionDTO.Response cambiarFechas(Long id, ReservacionDTO.FechasRequest req) {
        ReservacionModel r = buscar(id);
        if (r.getEstado() != EstadoReservacion.PENDIENTE && r.getEstado() != EstadoReservacion.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se pueden cambiar las fechas de reservaciones PENDIENTE o CONFIRMADA");
        }
        validarRango(req.fechaCheckin(), req.fechaCheckout());
        if (req.fechaCheckin().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de check-in no puede estar en el pasado");
        }
        if (hayTraslape(r.getPropiedad().getId_propiedad(), req.fechaCheckin(), req.fechaCheckout(), r.getId())) {
            throw new ReglaNegocioException("La propiedad ya está reservada en esas fechas", HttpStatus.CONFLICT);
        }

        BigDecimal nuevoMonto = calcularMonto(r.getPropiedad(), req.fechaCheckin(), req.fechaCheckout());
        BigDecimal yaPagado = pagoRepository.sumarPorReservacionExcepto(r.getId(), EstadoPago.CANCELADO);
        if (yaPagado != null && yaPagado.compareTo(nuevoMonto) > 0) {
            throw new ReglaNegocioException("El nuevo monto (" + nuevoMonto + ") es menor a lo ya pagado ("
                    + yaPagado + "); cancela primero los pagos sobrantes");
        }

        r.setFechaCheckin(req.fechaCheckin());
        r.setFechaCheckout(req.fechaCheckout());
        r.setMontoTotal(nuevoMonto);
        return ReservacionDTO.Response.from(reservacionRepository.saveAndFlush(r));
    }

    // ------------------------------------------------------------------ flujo de estados

    @Transactional
    public ReservacionDTO.Response confirmar(Long id) {
        return cambiarEstado(id, EstadoReservacion.PENDIENTE, EstadoReservacion.CONFIRMADA, "confirmar");
    }

    @Transactional
    public ReservacionDTO.Response checkIn(Long id) {
        return cambiarEstado(id, EstadoReservacion.CONFIRMADA, EstadoReservacion.ACTIVA, "hacer check-in de");
    }

    @Transactional
    public ReservacionDTO.Response checkOut(Long id) {
        ReservacionModel r = buscar(id);
        if (r.getEstado() != EstadoReservacion.ACTIVA) {
            throw new ReglaNegocioException("Solo se puede hacer check-out de una reservación ACTIVA");
        }
        r.setEstado(EstadoReservacion.FINALIZADA);
        r.setFechaSalida(LocalDate.now());
        return ReservacionDTO.Response.from(reservacionRepository.saveAndFlush(r));
    }

    /** Cancela la reservación y también sus pagos que aún no se han recibido. */
    @Transactional
    public ReservacionDTO.Response cancelar(Long id) {
        ReservacionModel r = buscar(id);
        if (r.getEstado() != EstadoReservacion.PENDIENTE && r.getEstado() != EstadoReservacion.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se pueden cancelar reservaciones PENDIENTE o CONFIRMADA");
        }
        r.setEstado(EstadoReservacion.CANCELADA);

        for (PagoModel pago : pagoRepository.buscarPorReservacion(id)) {
            if (pago.getEstadoPago() == EstadoPago.PENDIENTE || pago.getEstadoPago() == EstadoPago.NO_RECIBIDO) {
                pago.setEstadoPago(EstadoPago.CANCELADO);
                pagoRepository.save(pago);
            }
        }
        return ReservacionDTO.Response.from(reservacionRepository.saveAndFlush(r));
    }

    @Transactional
    public void eliminar(Long id) {
        ReservacionModel r = buscar(id);
        reservacionRepository.delete(r);
        reservacionRepository.flush(); // si tiene pagos, falla con 409
    }

    // ------------------------------------------------------------------ utilidades

    private ReservacionDTO.Response cambiarEstado(Long id, EstadoReservacion requerido, EstadoReservacion nuevo,
            String accion) {
        ReservacionModel r = buscar(id);
        if (r.getEstado() != requerido) {
            throw new ReglaNegocioException("Solo se puede " + accion + " una reservación en estado " + requerido
                    + " (actual: " + r.getEstado() + ")");
        }
        r.setEstado(nuevo);
        return ReservacionDTO.Response.from(reservacionRepository.saveAndFlush(r));
    }

    private ReservacionModel buscar(Long id) {
        return reservacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reservación", id));
    }

    private void validarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente", id);
        }
    }

    private ClienteModel clienteActivo(Long id, String rol) {
        ClienteModel c = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(rol, id));
        if (c.getEstatus() != ClienteStatus.ACTIVO) {
            throw new ReglaNegocioException(rol + " " + id + " no está ACTIVO (estatus: " + c.getEstatus() + ")");
        }
        return c;
    }

    private boolean hayTraslape(Long idPropiedad, LocalDate checkin, LocalDate checkout, Long excluirId) {
        return reservacionRepository.contarTraslapes(idPropiedad, ESTADOS_QUE_BLOQUEAN, checkin, checkout,
                excluirId) > 0;
    }

    private void validarRango(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new ReglaNegocioException("Debes indicar las fechas de check-in y check-out");
        }
        if (!checkout.isAfter(checkin)) {
            throw new ReglaNegocioException("La fecha de check-out debe ser posterior a la de check-in");
        }
    }

    private BigDecimal calcularMonto(PropiedadModel propiedad, LocalDate checkin, LocalDate checkout) {
        long noches = ChronoUnit.DAYS.between(checkin, checkout);
        return BigDecimal.valueOf(propiedad.getPrecio_noche())
                .multiply(BigDecimal.valueOf(noches))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
