# 🏢 Apartamentos Blue API

API REST para la administración de rentas de propiedades, desarrollada con **Spring Boot**. Permite gestionar clientes (propietarios, inquilinos y empresas), propiedades y sus habitaciones, reservaciones con control de fechas ocupadas, pagos con un flujo de estados, estados de cuenta y reportes de ocupación, ingresos y morosidad.

## Tabla de contenido

- [Descripción de la API](#descripción-de-la-api)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Variables de entorno](#variables-de-entorno)
- [Listado de endpoints](#listado-de-endpoints)
- [Reglas de negocio y validaciones](#reglas-de-negocio-y-validaciones)
- [Ejemplos de peticiones y respuestas](#ejemplos-de-peticiones-y-respuestas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Notas y limitaciones conocidas](#notas-y-limitaciones-conocidas)

---

## Descripción de la API

Apartamentos Blue expone los siguientes recursos:

| Recurso | Descripción |
|---------|-------------|
| **Clientes** | Personas o empresas registradas. Su tipo puede ser `PROPIETARIO`, `CLIENTE` (quien renta) o `EMPRESA`. Los propietarios pueden registrar una CLABE interbancaria para recibir transferencias. |
| **Propiedades** | Inmuebles disponibles para renta (casa, piso, departamento, loft, etc.), cada uno con un propietario, ubicación, precio por noche y características. |
| **Habitaciones** | Habitaciones individuales de una propiedad, con su propio estado (`DISPONIBLE`, `NO_DISPONIBLE`, `MANTENIMIENTO`). |
| **Reservaciones** | Renta de una propiedad entre dos fechas. Calcula el monto total (noches × precio por noche), evita traslapes de fechas y sigue un flujo de estados (pendiente → confirmada → activa → finalizada). |
| **Pagos** | Pagos asociados a una reservación, con método de pago y un flujo de estados (pendiente → recibido → confirmado). Incluye estados de cuenta para clientes y propietarios. |
| **Reportes** | Ocupación por propiedad, ingresos por periodo y morosidad. |

**URL base por defecto:** `http://localhost:1501`

Todos los endpoints trabajan con **JSON**. Los errores se devuelven con un formato JSON uniforme (ver [Formato de errores](#formato-de-errores)).

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 4.1.1 | Framework principal |
| Spring Web | — | API REST |
| Spring Data JPA (Hibernate) | — | Persistencia y repositorios |
| Spring Validation (Jakarta Validation) | — | Validación de datos de entrada |
| Spring Boot DevTools | — | Recarga automática en desarrollo |
| Thymeleaf | — | Dependencia incluida (actualmente sin vistas activas) |
| MySQL | 8+ | Base de datos relacional |
| MySQL Connector/J | — | Driver JDBC |
| Maven (Maven Wrapper) | — | Gestión de dependencias y construcción |

---

## Requisitos previos

- **JDK 21** o superior
- **MySQL 8** o superior en ejecución
- **Git**
- (Opcional) Maven; no es necesario porque el proyecto incluye el Maven Wrapper (`mvnw` / `mvnw.cmd`)

Verifica tu versión de Java:

```bash
java -version
```

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/mario-tapia-dev/apartamentos.git
cd apartamentos/apartamentos
```

El primer `apartamentos` es la carpeta del repositorio y el segundo es el proyecto Maven (donde están el `pom.xml` y el archivo `.env.example`). Los pasos siguientes se ejecutan desde esta carpeta.

### 2. Crear la base de datos

La aplicación crea las **tablas** automáticamente (`spring.jpa.hibernate.ddl-auto=update`), pero la **base de datos** debe existir previamente:

```sql
CREATE DATABASE apartamentos_blue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> ⚠️ **Si vienes de una versión anterior del proyecto:** las tablas `propiedades` y `clientes` pudieron quedar con columnas `enum(...)` con valores antiguos, y `ddl-auto=update` no las modifica. En ese caso reinicia la base de datos:
>
> ```sql
> DROP DATABASE apartamentos_blue;
> CREATE DATABASE apartamentos_blue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> ```

### 3. Configurar las credenciales

Las credenciales se leen de un archivo `.env` ubicado en la carpeta del proyecto (junto al `pom.xml`). Copia la plantilla incluida y edítala con tus datos:

**Linux / macOS**

```bash
cp .env.example .env
```

**Windows**

```bash
copy .env.example .env
```

Contenido del archivo `.env`:

```properties
DB_URL=jdbc:mysql://localhost:3306/apartamentos_blue
DB_USERNAME=root
DB_PASSWORD=tu_contraseña
```

> El archivo `.env` se busca en el **directorio desde el que se ejecuta la aplicación** (`spring.config.import=optional:file:.env[.properties]`). Si lo guardas en otra carpeta, la aplicación no lo encontrará.
>
> 🔒 Verifica que `.env` esté incluido en tu `.gitignore` para no subir tus credenciales al repositorio.

También puedes usar variables de entorno del sistema (consulta [Variables de entorno](#variables-de-entorno)).

### 4. Ejecutar la aplicación

Ejecuta el comando desde la carpeta del proyecto (la que contiene `pom.xml` y `.env`).

**Linux / macOS**

```bash
./mvnw spring-boot:run
```

**Windows**

```bash
mvnw.cmd spring-boot:run
```

La API quedará disponible en `http://localhost:1501`.

### 5. (Opcional) Generar y ejecutar el JAR

```bash
./mvnw clean package -DskipTests
java -jar target/apartamentos-0.0.1-SNAPSHOT.jar
```

Ejecuta el JAR desde la carpeta donde está el `.env`, o define las variables de entorno del sistema.

### 6. (Opcional) Ejecutar las pruebas

```bash
./mvnw test
```

> ⚠️ La prueba existente (`ApartamentosApplicationTests`) levanta el contexto completo de Spring, por lo que requiere MySQL en ejecución y las variables `DB_USERNAME` y `DB_PASSWORD` configuradas.

---

## Variables de entorno

| Variable | Obligatoria | Valor por defecto | Descripción |
|----------|:-----------:|-------------------|-------------|
| `DB_URL` | No | `jdbc:mysql://localhost:3306/apartamentos_blue` | URL JDBC de la base de datos MySQL |
| `DB_USERNAME` | **Sí** | — | Usuario de MySQL |
| `DB_PASSWORD` | **Sí** | — | Contraseña del usuario de MySQL |

Si falta `DB_USERNAME` o `DB_PASSWORD`, la aplicación no arrancará.

**Orden de prioridad:** las variables de entorno del sistema tienen prioridad sobre las del archivo `.env`.

### Configuración opcional

Cualquier propiedad de Spring Boot puede sobrescribirse con su variable de entorno equivalente.

| Variable | Propiedad | Valor por defecto | Descripción |
|----------|-----------|-------------------|-------------|
| `SERVER_PORT` | `server.port` | `1501` | Puerto de la API |
| `APP_PAGOS_DIAS_GRACIA` | `app.pagos.dias-gracia` | `3` | Días que se otorgan antes de considerar moroso un pago `PENDIENTE` o `NO_RECIBIDO` |

**Linux / macOS**

```bash
export DB_URL="jdbc:mysql://localhost:3306/apartamentos_blue"
export DB_USERNAME="root"
export DB_PASSWORD="tu_contraseña"

./mvnw spring-boot:run
```

**Windows (PowerShell)**

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/apartamentos_blue"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_contraseña"

.\mvnw.cmd spring-boot:run
```

> 🔒 No subas contraseñas reales al repositorio. Mantén `.env` fuera del control de versiones y comparte únicamente `.env.example`.

---

## Listado de endpoints

### Clientes — `/api/clientes`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/clientes` | Lista los clientes. Filtro opcional: `?tipo=PROPIETARIO` | `200`, `400` |
| `POST` | `/api/clientes` | Registra un cliente | `201`, `400`, `409` |
| `GET` | `/api/clientes/{id}` | Obtiene un cliente por ID | `200`, `404` (sin cuerpo) |
| `PUT` | `/api/clientes/{id}` | Actualiza un cliente (solo cambian los campos enviados) | `200`, `404`, `409` |
| `DELETE` | `/api/clientes/{id}` | Elimina un cliente | `204`, `404` (sin cuerpo), `409` |

### Propiedades — `/api/propiedades`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/propiedades` | Lista todas las propiedades | `200` |
| `POST` | `/api/propiedades` | Registra una propiedad | `201`, `400`, `404` |
| `GET` | `/api/propiedades/{id}` | Obtiene una propiedad por ID | `200`, `404` (sin cuerpo) |
| `PUT` | `/api/propiedades/{id}` | Actualiza una propiedad (se envía el objeto completo) | `200`, `400`, `404` |
| `DELETE` | `/api/propiedades/{id}` | Elimina una propiedad y sus habitaciones | `204`, `404`, `409` |
| `GET` | `/api/propiedades/tipo/{tipo}` | Lista propiedades por tipo | `200`, `400` |
| `GET` | `/api/propiedades/propietario/{idPropietario}` | Lista las propiedades de un propietario | `200`, `404` |

### Habitaciones — `/api/propiedades/{idPropiedad}/habitaciones`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/propiedades/{idPropiedad}/habitaciones` | Lista las habitaciones de una propiedad | `200`, `404` |
| `POST` | `/api/propiedades/{idPropiedad}/habitaciones` | Registra una habitación | `201`, `400`, `404`, `409` |
| `GET` | `/api/propiedades/{idPropiedad}/habitaciones/{numero}` | Obtiene una habitación | `200`, `404` |
| `PUT` | `/api/propiedades/{idPropiedad}/habitaciones/{numero}` | Cambia el estado de una habitación | `200`, `400`, `404` |
| `DELETE` | `/api/propiedades/{idPropiedad}/habitaciones/{numero}` | Elimina una habitación | `204`, `404` |

### Reservaciones — `/api/reservaciones`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/reservaciones` | Lista reservaciones. Filtro opcional: `?estado=CONFIRMADA` | `200`, `400` |
| `GET` | `/api/reservaciones/{id}` | Obtiene una reservación | `200`, `404` |
| `GET` | `/api/reservaciones/cliente/{idCliente}` | Reservaciones de un cliente (quien renta) | `200`, `404` |
| `GET` | `/api/reservaciones/arrendatario/{idArrendatario}` | Reservaciones de un propietario (quien recibe el pago) | `200`, `404` |
| `GET` | `/api/reservaciones/propiedad/{idPropiedad}` | Reservaciones de una propiedad | `200`, `404` |
| `GET` | `/api/reservaciones/propiedad/{idPropiedad}/disponible?checkin=&checkout=` | Indica si una propiedad está libre en un rango de fechas | `200`, `400`, `404` |
| `GET` | `/api/reservaciones/disponibles?checkin=&checkout=` | Lista las propiedades libres en un rango. Filtro opcional: `&tipo=CASA` | `200`, `400` |
| `POST` | `/api/reservaciones` | Crea una reservación | `201`, `400`, `404`, `409` |
| `PUT` | `/api/reservaciones/{id}/fechas` | Cambia las fechas de una reservación | `200`, `400`, `404`, `409` |
| `PATCH` | `/api/reservaciones/{id}/confirmar` | Confirma la reservación (`PENDIENTE` → `CONFIRMADA`) | `200`, `400`, `404` |
| `PATCH` | `/api/reservaciones/{id}/checkin` | Registra el check-in (`CONFIRMADA` → `ACTIVA`) | `200`, `400`, `404` |
| `PATCH` | `/api/reservaciones/{id}/checkout` | Registra el check-out (`ACTIVA` → `FINALIZADA`) | `200`, `400`, `404` |
| `PATCH` | `/api/reservaciones/{id}/cancelar` | Cancela la reservación y sus pagos sin cobrar | `200`, `400`, `404` |
| `DELETE` | `/api/reservaciones/{id}` | Elimina una reservación (sin pagos asociados) | `204`, `404`, `409` |

### Pagos — `/api/pagos`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/pagos` | Lista pagos. Filtro opcional: `?estado=PENDIENTE` | `200`, `400` |
| `GET` | `/api/pagos/{id}` | Obtiene un pago | `200`, `404` |
| `GET` | `/api/pagos/cliente/{idCliente}` | Pagos de un cliente | `200`, `404` |
| `GET` | `/api/pagos/arrendatario/{idArrendatario}` | Pagos recibidos por un propietario | `200`, `404` |
| `GET` | `/api/pagos/reservacion/{idReservacion}` | Pagos de una reservación | `200`, `404` |
| `GET` | `/api/pagos/estado-cuenta/cliente/{idCliente}` | Estado de cuenta de quien paga rentas | `200`, `404` |
| `GET` | `/api/pagos/estado-cuenta/arrendatario/{idArrendatario}` | Estado de cuenta de quien recibe pagos | `200`, `404` |
| `POST` | `/api/pagos` | Registra un pago | `201`, `400`, `404` |
| `PATCH` | `/api/pagos/{id}/estado` | Cambia el estado de un pago | `200`, `400`, `404` |
| `DELETE` | `/api/pagos/{id}` | Elimina un pago (solo `PENDIENTE` o `CANCELADO`) | `204`, `400`, `404` |

### Reportes — `/api/reportes`

| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| `GET` | `/api/reportes/ocupacion?desde=&hasta=` | Ocupación por propiedad y global | `200`, `400` |
| `GET` | `/api/reportes/ingresos?desde=&hasta=` | Ingresos (pagos confirmados) por periodo | `200`, `400` |
| `GET` | `/api/reportes/morosidad` | Clientes con pagos vencidos | `200` |

### Códigos de respuesta

| Código | Cuándo se devuelve |
|--------|--------------------|
| `200 OK` | Consulta o actualización exitosa |
| `201 Created` | Creación exitosa |
| `204 No Content` | Eliminación exitosa |
| `400 Bad Request` | Datos inválidos, regla de negocio incumplida, JSON malformado, fechas con formato incorrecto o valor de enum desconocido |
| `404 Not Found` | El recurso indicado no existe |
| `409 Conflict` | Traslape de fechas, habitación duplicada, valor único duplicado (email, INE) o registro con información relacionada que impide eliminarlo |

### Formato de errores

Casi todos los errores devuelven este JSON:

```json
{
  "timestamp": "2026-10-14T09:45:10.123",
  "status": 409,
  "mensaje": "La propiedad ya está reservada en esas fechas",
  "detalles": []
}
```

Los errores de validación incluyen en `detalles` un texto por cada campo inválido. La excepción son los `404` de `GET`/`DELETE` de clientes y `GET` de propiedades por ID, que se devuelven **sin cuerpo**.

### Valores permitidos (enums)

| Campo | Valores |
|-------|---------|
| `tipo` de cliente | `PROPIETARIO`, `CLIENTE`, `EMPRESA` |
| `estatus` de cliente | `ACTIVO`, `INACTIVO`, `SUSPENDIDO` |
| `tipo` de propiedad | `CASA`, `HABITACION`, `PISO`, `DEPARTAMENTO`, `ESTUDIO`, `LOFT` |
| `estado` de propiedad / `estadoHab` de habitación | `DISPONIBLE`, `NO_DISPONIBLE`, `MANTENIMIENTO` |
| `estado` de reservación | `PENDIENTE`, `CONFIRMADA`, `ACTIVA`, `FINALIZADA`, `CANCELADA` |
| `metodo` de pago | `EFECTIVO`, `TRANSFERENCIA`, `TARJETA`, `DEPOSITO` |
| `estadoPago` | `PENDIENTE`, `NO_RECIBIDO`, `RECIBIDO`, `CONFIRMADO`, `CANCELADO` |

Las fechas se envían en formato `AAAA-MM-DD`.

---

## Reglas de negocio y validaciones

### Clientes

- Obligatorios al crear: `tipo`, `nombres`, `apellidos`, `email` (formato válido y **único**) y `password`.
- `clabe` (opcional): debe tener exactamente **18 dígitos**.
- `ine` (opcional): debe ser único.
- `estatus` es `ACTIVO` por defecto y `tipo` es `CLIENTE` por defecto.
- `password` solo se acepta en las peticiones; **nunca se devuelve** en las respuestas.
- En `PUT` solo se modifican los campos que se envían; los omitidos conservan su valor. Una `clabe` vacía (`""`) la borra.

### Propiedades

- Obligatorios: `propietario` (como `{"id": N}`), `tipo`, `titulo`, `descripcion`, `direccion`, `ciudad`, `estidadFederativa`, `pais`.
- `precio_noche`, `capacidad` y `num_habitaciones` deben ser mayores a 0.
- El propietario debe existir y ser de tipo `PROPIETARIO` o `EMPRESA` (un cliente de tipo `CLIENTE` no puede ser dueño).
- Los nombres de los campos de propiedades se envían **tal como aparecen en los ejemplos** (mezclan `camelCase` y `snake_case`, e incluyen `estidadFederativa`).
- Si en un `PUT` omites `estado`, se asume `DISPONIBLE`.
- Una propiedad con reservaciones no puede eliminarse (`409`). Sus habitaciones se eliminan junto con ella.

### Habitaciones

- `numeroHabitacion` debe ser mayor o igual a 1 y **no mayor** que `num_habitaciones` de la propiedad.
- No puede repetirse el número de habitación dentro de una propiedad (`409`).
- Si no se indica `estadoHab`, se asigna `DISPONIBLE`.

### Reservaciones

- Campos: `idPropiedad`, `idCliente`, `fechaCheckin` y `fechaCheckout` obligatorios; `idArrendatario` opcional (si se omite se usa el propietario de la propiedad).
- `fechaCheckout` debe ser posterior a `fechaCheckin`, y el check-in no puede estar en el pasado.
- La propiedad debe estar en estado `DISPONIBLE`.
- El arrendatario debe ser el propietario de la propiedad, y el cliente no puede reservar su propia propiedad.
- Cliente y arrendatario deben tener estatus `ACTIVO`.
- **Traslapes:** las reservaciones `PENDIENTE`, `CONFIRMADA` y `ACTIVA` bloquean fechas. El check-in cuenta como noche ocupada y el check-out no (una reservación puede empezar el día que termina otra). Un traslape devuelve `409`.
- `montoTotal` = noches × `precio_noche`, con 2 decimales.
- Las fechas solo pueden cambiarse en reservaciones `PENDIENTE` o `CONFIRMADA`, y el nuevo monto no puede ser menor a lo ya pagado.
- Al cancelar, los pagos `PENDIENTE` y `NO_RECIBIDO` pasan a `CANCELADO`.
- Una reservación con pagos asociados no puede eliminarse (`409`).

**Flujo de estados de una reservación**

```text
PENDIENTE ──confirmar──▶ CONFIRMADA ──checkin──▶ ACTIVA ──checkout──▶ FINALIZADA
    │                        │
    └──────── cancelar ──────┴──▶ CANCELADA
```

Al hacer check-out se registra `fechaSalida` con la fecha actual. Además, una reservación `PENDIENTE` pasa a `CONFIRMADA` automáticamente cuando uno de sus pagos se confirma.

### Pagos

- `idReservacion` y `metodo` son obligatorios; `montoTotal` es opcional (si se omite se cobra el saldo pendiente de la reservación).
- No se pueden registrar pagos de reservaciones `CANCELADA`.
- La suma de pagos no cancelados no puede exceder el monto de la reservación.
- Para pagar por `TRANSFERENCIA`, el propietario debe tener una CLABE registrada.
- Al confirmar un pago se guarda `fechaConfirmacion`.
- Solo se pueden eliminar pagos `PENDIENTE` o `CANCELADO`; el resto se conserva como historial.

**Flujo de estados de un pago**

```text
PENDIENTE   ──▶ RECIBIDO | NO_RECIBIDO | CANCELADO
NO_RECIBIDO ──▶ RECIBIDO | CANCELADO
RECIBIDO    ──▶ CONFIRMADO | NO_RECIBIDO | CANCELADO
CONFIRMADO y CANCELADO son estados finales
```

### Estados de cuenta

| Campo | Significado |
|-------|-------------|
| `totalReservaciones` | Suma de reservaciones no canceladas |
| `totalConfirmado` | Pagos `CONFIRMADO` |
| `totalPorConfirmar` | Pagos `RECIBIDO` |
| `totalPendiente` | Pagos `PENDIENTE` y `NO_RECIBIDO` |
| `saldo` | `totalReservaciones` − `totalConfirmado` |

### Reportes

- **Ocupación:** periodo `[desde, hasta)`, donde `hasta` no cuenta como noche. Considera reservaciones `CONFIRMADA`, `ACTIVA` y `FINALIZADA`. Si no se envían fechas, usa el mes actual.
- **Ingresos:** suma de pagos `CONFIRMADO` cuya fecha de confirmación cae entre `desde` y `hasta` (ambas inclusive). Si no se envían fechas, va del día 1 del mes a hoy.
- **Morosidad:** pagos `PENDIENTE` o `NO_RECIBIDO` creados hace más de `app.pagos.dias-gracia` días (3 por defecto).

---

## Ejemplos de peticiones y respuestas

Los ejemplos usan `curl`, la URL base `http://localhost:1501` y siguen un flujo completo: propietario → cliente → propiedad → reservación → pago → reportes. Las fechas de ejemplo suponen que hoy es el **2026-10-14**; recuerda que el check-in no puede estar en el pasado.

### Clientes

#### Crear un propietario

```bash
curl -X POST http://localhost:1501/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "PROPIETARIO",
    "nombres": "Ana",
    "apellidos": "Ruiz",
    "email": "ana@mail.com",
    "telefono": "2291234567",
    "clabe": "012345678901234567",
    "password": "1234"
  }'
```

**Respuesta** `201 Created`

```json
{
  "id": 1,
  "tipo": "PROPIETARIO",
  "nombres": "Ana",
  "apellidos": "Ruiz",
  "email": "ana@mail.com",
  "telefono": "2291234567",
  "fechaNacimiento": null,
  "ine": null,
  "clabe": "012345678901234567",
  "direccion": null,
  "fechaRegistro": "2026-10-14T09:30:12.345",
  "estatus": "ACTIVO"
}
```

> `password` no aparece en la respuesta.

#### Crear un cliente que renta

```bash
curl -X POST http://localhost:1501/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "CLIENTE",
    "nombres": "Luis",
    "apellidos": "Pérez",
    "email": "luis@mail.com",
    "password": "1234"
  }'
```

**Respuesta** `201 Created`: el cliente creado, con `id` 2.

**Respuesta con datos inválidos** `400 Bad Request`

```json
{
  "timestamp": "2026-10-14T09:31:02.118",
  "status": 400,
  "mensaje": "Datos inválidos",
  "detalles": [
    "clabe: La CLABE debe tener exactamente 18 dígitos"
  ]
}
```

> El texto de los mensajes de validación estándar (por ejemplo, campo obligatorio vacío) depende del idioma configurado en el sistema.

**Respuesta con email duplicado** `409 Conflict`

```json
{
  "timestamp": "2026-10-14T09:31:40.552",
  "status": 409,
  "mensaje": "Operación rechazada por la base de datos: valor duplicado (email, INE...) o el registro tiene información relacionada",
  "detalles": []
}
```

#### Listar clientes (con filtro opcional)

```bash
curl http://localhost:1501/api/clientes
curl "http://localhost:1501/api/clientes?tipo=PROPIETARIO"
```

**Respuesta** `200 OK`: arreglo de clientes con la misma estructura del ejemplo anterior.

#### Obtener un cliente por ID

```bash
curl http://localhost:1501/api/clientes/1
```

**Respuesta** `200 OK`: el cliente. Si no existe, `404 Not Found` sin cuerpo.

#### Actualizar un cliente

Solo se envían los campos a modificar.

```bash
curl -X PUT http://localhost:1501/api/clientes/2 \
  -H "Content-Type: application/json" \
  -d '{ "telefono": "2297654321" }'
```

**Respuesta** `200 OK`

```json
{
  "id": 2,
  "tipo": "CLIENTE",
  "nombres": "Luis",
  "apellidos": "Pérez",
  "email": "luis@mail.com",
  "telefono": "2297654321",
  "fechaNacimiento": null,
  "ine": null,
  "clabe": null,
  "direccion": null,
  "fechaRegistro": "2026-10-14T09:32:20.870",
  "estatus": "ACTIVO"
}
```

#### Eliminar un cliente

```bash
curl -X DELETE http://localhost:1501/api/clientes/2 -i
```

**Respuesta** `204 No Content`. Si el cliente tiene registros relacionados (propiedades, reservaciones o pagos), se devuelve `409 Conflict`.

---

### Propiedades

#### Crear una propiedad

El propietario se indica como `"propietario": { "id": 1 }`.

```bash
curl -X POST http://localhost:1501/api/propiedades \
  -H "Content-Type: application/json" \
  -d '{
    "propietario": { "id": 1 },
    "tipo": "PISO",
    "titulo": "Piso en el centro",
    "descripcion": "2 recámaras",
    "direccion": "Calle 1 #10",
    "ciudad": "Veracruz",
    "estidadFederativa": "Veracruz",
    "codigo_postal": 91700,
    "pais": "México",
    "latitud": 19.17,
    "longitud": -96.13,
    "precio_noche": 800,
    "capacidad": 4,
    "num_habitaciones": 2,
    "num_banos": 1,
    "metro_cuadrados": 70
  }'
```

**Respuesta** `201 Created`

```json
{
  "id_propiedad": 1,
  "propietario": {
    "id": 1,
    "tipo": "PROPIETARIO",
    "nombres": "Ana",
    "apellidos": "Ruiz",
    "email": "ana@mail.com",
    "telefono": "2291234567",
    "fechaNacimiento": null,
    "ine": null,
    "clabe": "012345678901234567",
    "direccion": null,
    "fechaRegistro": "2026-10-14T09:30:12.345",
    "estatus": "ACTIVO"
  },
  "tipo": "PISO",
  "titulo": "Piso en el centro",
  "descripcion": "2 recámaras",
  "direccion": "Calle 1 #10",
  "ciudad": "Veracruz",
  "estidadFederativa": "Veracruz",
  "codigo_postal": 91700,
  "pais": "México",
  "latitud": 19.17,
  "longitud": -96.13,
  "precio_noche": 800.0,
  "capacidad": 4,
  "num_habitaciones": 2,
  "num_banos": 1,
  "metro_cuadrados": 70,
  "comodidades": null,
  "reglas": null,
  "estado": "DISPONIBLE",
  "fecha_registro": "2026-10-14T09:35:00.210",
  "fecha_actualizacion": "2026-10-14T09:35:00.210"
}
```

**Respuesta si el propietario es de tipo `CLIENTE`** `400 Bad Request`

```json
{
  "timestamp": "2026-10-14T09:36:11.004",
  "status": 400,
  "mensaje": "El cliente 2 es de tipo CLIENTE; para ser dueño de una propiedad debe ser PROPIETARIO o EMPRESA",
  "detalles": []
}
```

**Respuesta si el propietario no existe** `404 Not Found`

```json
{
  "timestamp": "2026-10-14T09:36:40.771",
  "status": 404,
  "mensaje": "Propietario con id 99 no encontrado(a)",
  "detalles": []
}
```

#### Consultas

```bash
curl http://localhost:1501/api/propiedades
curl http://localhost:1501/api/propiedades/1
curl http://localhost:1501/api/propiedades/tipo/PISO
curl http://localhost:1501/api/propiedades/propietario/1
```

**Respuesta** `200 OK`: una propiedad o un arreglo de propiedades, con la estructura del ejemplo anterior.

#### Actualizar una propiedad

Se debe enviar el objeto completo (los campos obligatorios se validan igual que al crear).

```bash
curl -X PUT http://localhost:1501/api/propiedades/1 \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "PISO",
    "titulo": "Piso en el centro (remodelado)",
    "descripcion": "2 recámaras, cocina nueva",
    "direccion": "Calle 1 #10",
    "ciudad": "Veracruz",
    "estidadFederativa": "Veracruz",
    "codigo_postal": 91700,
    "pais": "México",
    "latitud": 19.17,
    "longitud": -96.13,
    "precio_noche": 900,
    "capacidad": 4,
    "num_habitaciones": 2,
    "num_banos": 1,
    "metro_cuadrados": 70,
    "estado": "DISPONIBLE"
  }'
```

**Respuesta** `200 OK`: la propiedad actualizada.

#### Eliminar una propiedad

```bash
curl -X DELETE http://localhost:1501/api/propiedades/1 -i
```

**Respuesta** `204 No Content`. Si tiene reservaciones, `409 Conflict`; si no existe, `404 Not Found`.

---

### Habitaciones

#### Registrar una habitación

```bash
curl -X POST http://localhost:1501/api/propiedades/1/habitaciones \
  -H "Content-Type: application/json" \
  -d '{ "numeroHabitacion": 1, "estadoHab": "DISPONIBLE" }'
```

**Respuesta** `201 Created`

```json
{
  "idPropiedad": 1,
  "numeroHabitacion": 1,
  "estadoHab": "DISPONIBLE"
}
```

**Respuesta si el número excede `num_habitaciones`** `400 Bad Request`

```json
{
  "timestamp": "2026-10-14T09:40:02.390",
  "status": 400,
  "mensaje": "La propiedad solo tiene 2 habitación(es); el número 5 no es válido",
  "detalles": []
}
```

**Respuesta si la habitación ya existe** `409 Conflict`

```json
{
  "timestamp": "2026-10-14T09:40:30.112",
  "status": 409,
  "mensaje": "La habitación 1 ya está registrada en la propiedad 1",
  "detalles": []
}
```

#### Consultar, cambiar estado y eliminar

```bash
curl http://localhost:1501/api/propiedades/1/habitaciones
curl http://localhost:1501/api/propiedades/1/habitaciones/1

curl -X PUT http://localhost:1501/api/propiedades/1/habitaciones/1 \
  -H "Content-Type: application/json" \
  -d '{ "estadoHab": "MANTENIMIENTO" }'

curl -X DELETE http://localhost:1501/api/propiedades/1/habitaciones/1 -i
```

**Respuesta del `PUT`** `200 OK`

```json
{
  "idPropiedad": 1,
  "numeroHabitacion": 1,
  "estadoHab": "MANTENIMIENTO"
}
```

El `DELETE` responde `204 No Content`.

---

### Reservaciones

#### Consultar disponibilidad

```bash
curl "http://localhost:1501/api/reservaciones/propiedad/1/disponible?checkin=2026-11-10&checkout=2026-11-14"
```

**Respuesta** `200 OK`

```json
{
  "idPropiedad": 1,
  "checkin": "2026-11-10",
  "checkout": "2026-11-14",
  "disponible": true
}
```

Para listar todas las propiedades libres en un rango (con filtro opcional por tipo):

```bash
curl "http://localhost:1501/api/reservaciones/disponibles?checkin=2026-11-10&checkout=2026-11-14&tipo=PISO"
```

**Respuesta** `200 OK`: arreglo de propiedades (misma estructura que en la sección de propiedades).

#### Crear una reservación

`idArrendatario` es opcional; si se omite se usa el propietario de la propiedad.

```bash
curl -X POST http://localhost:1501/api/reservaciones \
  -H "Content-Type: application/json" \
  -d '{
    "idPropiedad": 1,
    "idCliente": 2,
    "fechaCheckin": "2026-11-10",
    "fechaCheckout": "2026-11-14"
  }'
```

**Respuesta** `201 Created`

```json
{
  "id": 1,
  "idPropiedad": 1,
  "propiedad": "Piso en el centro",
  "idCliente": 2,
  "cliente": "Luis Pérez",
  "idArrendatario": 1,
  "arrendatario": "Ana Ruiz",
  "fechaCreacion": "2026-10-14",
  "fechaCheckin": "2026-11-10",
  "fechaCheckout": "2026-11-14",
  "fechaSalida": null,
  "estado": "PENDIENTE",
  "montoTotal": 3200.00
}
```

> El monto corresponde a 4 noches × $800.00.

**Respuesta si las fechas ya están ocupadas** `409 Conflict`

```json
{
  "timestamp": "2026-10-14T09:45:10.123",
  "status": 409,
  "mensaje": "La propiedad ya está reservada en esas fechas",
  "detalles": []
}
```

**Otros mensajes de error** `400 Bad Request` (campo `mensaje`)

```text
La fecha de check-out debe ser posterior a la de check-in
La fecha de check-in no puede estar en el pasado
La propiedad no se puede reservar (estado: MANTENIMIENTO)
El cliente no puede reservar su propia propiedad
Cliente 2 no está ACTIVO (estatus: SUSPENDIDO)
```

#### Consultas

```bash
curl http://localhost:1501/api/reservaciones
curl "http://localhost:1501/api/reservaciones?estado=CONFIRMADA"
curl http://localhost:1501/api/reservaciones/1
curl http://localhost:1501/api/reservaciones/cliente/2
curl http://localhost:1501/api/reservaciones/arrendatario/1
curl http://localhost:1501/api/reservaciones/propiedad/1
```

**Respuesta** `200 OK`: una reservación o un arreglo con la estructura del ejemplo anterior.

#### Cambiar las fechas

```bash
curl -X PUT http://localhost:1501/api/reservaciones/1/fechas \
  -H "Content-Type: application/json" \
  -d '{ "fechaCheckin": "2026-11-12", "fechaCheckout": "2026-11-16" }'
```

**Respuesta** `200 OK`: la reservación con las nuevas fechas y el monto recalculado.

#### Cambiar el estado de una reservación

```bash
curl -X PATCH http://localhost:1501/api/reservaciones/1/confirmar
curl -X PATCH http://localhost:1501/api/reservaciones/1/checkin
curl -X PATCH http://localhost:1501/api/reservaciones/1/checkout
curl -X PATCH http://localhost:1501/api/reservaciones/1/cancelar
```

**Respuesta** `200 OK`: la reservación con su nuevo `estado` (y `fechaSalida` tras el check-out).

**Respuesta si el estado actual no lo permite** `400 Bad Request`

```json
{
  "timestamp": "2026-10-14T09:50:44.601",
  "status": 400,
  "mensaje": "Solo se puede hacer check-in de una reservación en estado CONFIRMADA (actual: PENDIENTE)",
  "detalles": []
}
```

#### Eliminar una reservación

```bash
curl -X DELETE http://localhost:1501/api/reservaciones/1 -i
```

**Respuesta** `204 No Content`. Si tiene pagos asociados, `409 Conflict`.

---

### Pagos

#### Registrar un pago

Sin `montoTotal`, se cobra el saldo pendiente de la reservación.

```bash
curl -X POST http://localhost:1501/api/pagos \
  -H "Content-Type: application/json" \
  -d '{
    "idReservacion": 1,
    "metodo": "TRANSFERENCIA"
  }'
```

**Respuesta** `201 Created`

```json
{
  "id": 1,
  "idCliente": 2,
  "cliente": "Luis Pérez",
  "idArrendatario": 1,
  "arrendatario": "Ana Ruiz",
  "idReservacion": 1,
  "estadoPago": "PENDIENTE",
  "fechaCreacion": "2026-10-14",
  "fechaConfirmacion": null,
  "metodo": "TRANSFERENCIA",
  "montoTotal": 3200.00
}
```

Para un pago parcial, envía el monto:

```bash
curl -X POST http://localhost:1501/api/pagos \
  -H "Content-Type: application/json" \
  -d '{ "idReservacion": 1, "metodo": "EFECTIVO", "montoTotal": 1000 }'
```

**Mensajes de error** `400 Bad Request` (campo `mensaje`)

```text
No se pueden registrar pagos de una reservación CANCELADA
La reservación ya tiene pagos por el total (3200.00)
El monto (5000) excede el saldo por pagar (3200.00)
El propietario (id 1) no tiene CLABE registrada, no se puede pagar por transferencia
```

#### Cambiar el estado de un pago

```bash
curl -X PATCH http://localhost:1501/api/pagos/1/estado \
  -H "Content-Type: application/json" \
  -d '{ "estado": "RECIBIDO" }'

curl -X PATCH http://localhost:1501/api/pagos/1/estado \
  -H "Content-Type: application/json" \
  -d '{ "estado": "CONFIRMADO" }'
```

**Respuesta de la segunda petición** `200 OK`

```json
{
  "id": 1,
  "idCliente": 2,
  "cliente": "Luis Pérez",
  "idArrendatario": 1,
  "arrendatario": "Ana Ruiz",
  "idReservacion": 1,
  "estadoPago": "CONFIRMADO",
  "fechaCreacion": "2026-10-14",
  "fechaConfirmacion": "2026-10-14",
  "metodo": "TRANSFERENCIA",
  "montoTotal": 3200.00
}
```

> Al confirmar el pago, la reservación `PENDIENTE` pasa automáticamente a `CONFIRMADA`.

**Respuesta si la transición no está permitida** `400 Bad Request`

```json
{
  "timestamp": "2026-10-14T10:02:15.330",
  "status": 400,
  "mensaje": "Transición no permitida: CONFIRMADO -> CANCELADO",
  "detalles": []
}
```

#### Consultas

```bash
curl http://localhost:1501/api/pagos
curl "http://localhost:1501/api/pagos?estado=PENDIENTE"
curl http://localhost:1501/api/pagos/1
curl http://localhost:1501/api/pagos/cliente/2
curl http://localhost:1501/api/pagos/arrendatario/1
curl http://localhost:1501/api/pagos/reservacion/1
```

**Respuesta** `200 OK`: un pago o un arreglo con la estructura del ejemplo anterior.

#### Estado de cuenta

```bash
curl http://localhost:1501/api/pagos/estado-cuenta/cliente/2
curl http://localhost:1501/api/pagos/estado-cuenta/arrendatario/1
```

**Respuesta** `200 OK` (ejemplo para el cliente 2 con el pago ya confirmado)

```json
{
  "idCliente": 2,
  "nombre": "Luis Pérez",
  "rol": "CLIENTE",
  "totalReservaciones": 3200.00,
  "totalConfirmado": 3200.00,
  "totalPorConfirmar": 0,
  "totalPendiente": 0,
  "saldo": 0.00,
  "pagos": [
    {
      "id": 1,
      "idCliente": 2,
      "cliente": "Luis Pérez",
      "idArrendatario": 1,
      "arrendatario": "Ana Ruiz",
      "idReservacion": 1,
      "estadoPago": "CONFIRMADO",
      "fechaCreacion": "2026-10-14",
      "fechaConfirmacion": "2026-10-14",
      "metodo": "TRANSFERENCIA",
      "montoTotal": 3200.00
    }
  ]
}
```

#### Eliminar un pago

```bash
curl -X DELETE http://localhost:1501/api/pagos/1 -i
```

**Respuesta** `204 No Content`. Solo se pueden eliminar pagos `PENDIENTE` o `CANCELADO`; en otro caso se devuelve `400 Bad Request`.

---

### Reportes

#### Ocupación

```bash
curl "http://localhost:1501/api/reportes/ocupacion?desde=2026-11-01&hasta=2026-12-01"
```

**Respuesta** `200 OK`

```json
{
  "desde": "2026-11-01",
  "hasta": "2026-12-01",
  "totalPropiedades": 1,
  "nochesOcupadas": 4,
  "nochesDisponiblesTotales": 30,
  "ocupacionGlobal": 13.33,
  "propiedades": [
    {
      "idPropiedad": 1,
      "titulo": "Piso en el centro",
      "tipo": "PISO",
      "nochesOcupadas": 4,
      "nochesPeriodo": 30,
      "porcentajeOcupacion": 13.33
    }
  ]
}
```

#### Ingresos

```bash
curl "http://localhost:1501/api/reportes/ingresos?desde=2026-10-01&hasta=2026-10-31"
```

**Respuesta** `200 OK`

```json
{
  "desde": "2026-10-01",
  "hasta": "2026-10-31",
  "numeroPagos": 1,
  "totalIngresos": 3200.00,
  "porMetodo": {
    "TRANSFERENCIA": 3200.00
  },
  "porPropietario": [
    {
      "idPropietario": 1,
      "propietario": "Ana Ruiz",
      "numeroPagos": 1,
      "total": 3200.00
    }
  ]
}
```

#### Morosidad

```bash
curl http://localhost:1501/api/reportes/morosidad
```

**Respuesta** `200 OK` (ejemplo ilustrativo con un pago pendiente creado el 2026-10-07)

```json
{
  "fechaCorte": "2026-10-14",
  "diasGracia": 3,
  "totalAdeudado": 1600.00,
  "deudores": [
    {
      "idCliente": 2,
      "nombre": "Luis Pérez",
      "email": "luis@mail.com",
      "telefono": "2297654321",
      "pagosVencidos": 1,
      "montoAdeudado": 1600.00,
      "diasMaximoAtraso": 4
    }
  ]
}
```

---

## Estructura del proyecto

```text
apartamentos/                                    # Raíz del repositorio
└── apartamentos/                                # Proyecto Maven
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    ├── .env.example                             # Plantilla de credenciales
    ├── .env                                     # Credenciales locales (no deben versionarse)
    ├── GUIA_API.md                              # Guía rápida de la API
    ├── LOGA-apartamentos.png                    # Diagrama entidad-relación de diseño
    └── src/
        ├── main/
        │   ├── java/com/blue/apartamentos/
        │   │   ├── ApartamentosApplication.java
        │   │   ├── controllers/                 # Endpoints REST
        │   │   ├── services/                    # Lógica de negocio
        │   │   ├── repositories/                # Repositorios Spring Data JPA
        │   │   ├── models/                      # Entidades JPA
        │   │   ├── dtos/                        # Objetos de petición y respuesta
        │   │   ├── enums/                       # Tipos y estados
        │   │   └── exceptions/                  # Excepciones y manejo global de errores
        │   └── resources/
        │       ├── application.properties
        │       └── templates/clientes/          # Plantillas Thymeleaf (sin controlador)
        └── test/
```

### Modelo de datos

- **clientes**: `id_cliente` (PK), `tipo_cliente`, `nombre`, `apellidos`, `email` (único), `telefono`, `fecha_nacimiento`, `numero_ine` (único), `clabe`, `direccion`, `fecha_registro`, `estatus`, `password`
- **propiedades**: `id_propiedad` (PK), `id_propietario` (FK → clientes), `tipo_propiedad`, `titulo`, `descripcion`, `direccion`, `ciudad`, `estado`, `codigo_postal`, `pais`, `latitud`, `longitud`, `precio_noche`, `capacidad`, `num_habitaciones`, `num_banos`, `metro_cuadrados`, `comodidades`, `reglas`, `estado_hab`, `fecha_registro`, `fecha_actualizacion`
- **detalle_propiedad**: PK compuesta (`id_propiedad` FK → propiedades, `numero_habitacion`), `estado_hab`
- **reservaciones**: `id_reservacion` (PK), `id_propiedad` (FK), `id_cliente` (FK), `id_arrendatario` (FK → clientes), `fecha_creacion`, `fecha_checkin`, `fecha_checkout`, `fecha_salida`, `estado_reservacion`, `monto_total`
- **pagos**: `id_pago` (PK), `id_cliente` (FK), `id_arrendatario` (FK), `id_reservacion` (FK), `estado_pago`, `fecha_creacion`, `fecha_confirmacion`, `metodo`, `monto_total`

---

## Notas y limitaciones conocidas

- **Sin autenticación ni autorización:** todos los endpoints son públicos y no existe un endpoint de inicio de sesión.
- **Contraseñas sin cifrar:** `password` se guarda en texto plano en la base de datos; solo se oculta en las respuestas JSON. Para un entorno real conviene cifrarla (por ejemplo, con BCrypt).
- **Interfaz web inactiva:** el proyecto incluye Thymeleaf y las plantillas `clientes/list.html` y `clientes/form.html`, pero no hay ningún controlador que las sirva, por lo que hoy solo se puede usar la API REST.
- **`404` sin cuerpo:** `GET /api/clientes/{id}`, `DELETE /api/clientes/{id}` y `GET /api/propiedades/{id}` devuelven `404` sin el JSON de error cuando el registro no existe; el resto de los recursos sí devuelve el formato de error estándar.
- **Nombres de campo de propiedades:** mezclan `camelCase` y `snake_case` (`estidadFederativa`, `precio_noche`, `fecha_registro`…) y deben enviarse exactamente así.
- **Habitaciones y reservaciones:** las reservaciones se hacen sobre la propiedad completa; el estado de cada habitación no se tiene en cuenta al reservar (solo el estado de la propiedad).
- **Base de datos existente:** `ddl-auto=update` no modifica columnas `enum` ya creadas; si cambian los valores de un enum hay que migrar o recrear la base de datos.
- **Registro de SQL:** `spring.jpa.show-sql=true` imprime las consultas en consola; desactívalo en producción.
