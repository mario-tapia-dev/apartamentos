# Guía rápida de la API (puerto 1501)

> **Antes de arrancar por primera vez:** las tablas `propiedades` y `clientes` de tu BD anterior tienen
> columnas `enum(...)` con los valores viejos (ej. `tipo_propiedad` con ACTIVO/INACTIVO/SUSPENDIDO) y
> `ddl-auto=update` no las modifica. Reinicia la BD:
> `DROP DATABASE apartamentos_blue; CREATE DATABASE apartamentos_blue;`

## Orden sugerido para probar

1. **Propietario** — `POST /api/clientes`
```json
{ "tipo": "PROPIETARIO", "nombres": "Ana", "apellidos": "Ruiz", "email": "ana@mail.com",
  "telefono": "2291234567", "clabe": "012345678901234567", "password": "1234" }
```
2. **Cliente que renta** — `POST /api/clientes`
```json
{ "tipo": "CLIENTE", "nombres": "Luis", "apellidos": "Pérez", "email": "luis@mail.com", "password": "1234" }
```
3. **Propiedad** — `POST /api/propiedades`
```json
{ "propietario": { "id": 1 }, "tipo": "PISO", "titulo": "Piso en el centro",
  "descripcion": "2 recámaras", "direccion": "Calle 1 #10", "ciudad": "Veracruz",
  "estidadFederativa": "Veracruz", "codigo_postal": 91700, "pais": "México",
  "latitud": 19.17, "longitud": -96.13, "precio_noche": 800, "capacidad": 4,
  "num_habitaciones": 2, "num_banos": 1, "metro_cuadrados": 70 }
```
4. **Habitaciones** — `POST /api/propiedades/1/habitaciones` → `{ "numeroHabitacion": 1, "estadoHab": "DISPONIBLE" }`
5. **Disponibilidad** — `GET /api/reservaciones/disponibles?checkin=2026-10-01&checkout=2026-10-05`
6. **Reservar** — `POST /api/reservaciones`
```json
{ "idPropiedad": 1, "idCliente": 2, "fechaCheckin": "2026-10-01", "fechaCheckout": "2026-10-05" }
```
7. **Pago** — `POST /api/pagos` → `{ "idReservacion": 1, "metodo": "TRANSFERENCIA" }` (sin monto = cobra el saldo)
8. **Flujo del pago** — `PATCH /api/pagos/1/estado` → `{ "estado": "RECIBIDO" }` y luego `{ "estado": "CONFIRMADO" }`
   (al confirmar, la reservación pasa de PENDIENTE a CONFIRMADA)
9. **Estados de cuenta** — `GET /api/pagos/estado-cuenta/cliente/2` y `/arrendatario/1`
10. **Reportes**
    - `GET /api/reportes/ocupacion?desde=2026-10-01&hasta=2026-11-01`
    - `GET /api/reportes/ingresos?desde=2026-10-01&hasta=2026-10-31`
    - `GET /api/reportes/morosidad`

## Endpoints

| Recurso | Endpoints |
|---|---|
| Clientes | `GET/POST /api/clientes` (`?tipo=`), `GET/PUT/DELETE /api/clientes/{id}` |
| Propiedades | `GET/POST /api/propiedades`, `GET/PUT/DELETE /{id}`, `GET /tipo/{tipo}`, `GET /propietario/{id}` |
| Habitaciones | `GET/POST /api/propiedades/{id}/habitaciones`, `GET/PUT/DELETE .../{numero}` |
| Reservaciones | `GET/POST /api/reservaciones` (`?estado=`), `GET/DELETE /{id}`, `PUT /{id}/fechas`, `PATCH /{id}/confirmar|checkin|checkout|cancelar`, `GET /cliente/{id}`, `/arrendatario/{id}`, `/propiedad/{id}`, `/propiedad/{id}/disponible`, `/disponibles` |
| Pagos | `GET/POST /api/pagos` (`?estado=`), `GET/DELETE /{id}`, `PATCH /{id}/estado`, `GET /cliente/{id}`, `/arrendatario/{id}`, `/reservacion/{id}`, `/estado-cuenta/cliente/{id}`, `/estado-cuenta/arrendatario/{id}` |
| Reportes | `GET /api/reportes/ocupacion|ingresos|morosidad` |

## Flujo permitido de un pago
`PENDIENTE → RECIBIDO | NO_RECIBIDO | CANCELADO` · `NO_RECIBIDO → RECIBIDO | CANCELADO` ·
`RECIBIDO → CONFIRMADO | NO_RECIBIDO | CANCELADO` · `CONFIRMADO` y `CANCELADO` son finales.

## Morosidad
Un pago PENDIENTE / NO_RECIBIDO se considera vencido después de `app.pagos.dias-gracia` días
(configurable en `application.properties`, por defecto 3).
