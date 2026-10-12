package com.blue.apartamentos.exceptions;

import org.springframework.http.HttpStatus;

/** Se lanza cuando se viola una regla de negocio (fechas inválidas, traslape de reservas, etc.). */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus status;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, HttpStatus.BAD_REQUEST);
    }

    public ReglaNegocioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
