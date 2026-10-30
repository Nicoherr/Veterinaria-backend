# VetSM — Backend con JWT (Spring Boot)

## Descripción general

VetSM Backend es una API REST para la gestión de una clínica veterinaria. Permite registrar usuarios e iniciar sesión, así como administrar mascotas y citas veterinarias asociadas al usuario autenticado. La aplicación está desarrollada con Spring Boot y utiliza JSON Web Tokens (JWT) para proteger los recursos.

## Características principales

- **Autenticación JWT stateless:** los tokens se envían en el encabezado `Authorization: Bearer <token>`; no se mantienen sesiones en el servidor.
- **Seguridad con Spring Security:** `JwtAuthenticationFilter` valida el token y establece la identidad del usuario en cada solicitud protegida.
- **Manejo global de excepciones:** `GlobalExceptionHandler`, implementado con `@RestControllerAdvice`, normaliza las respuestas de error.
- **Base de datos MySQL:** se utiliza en desarrollo, pruebas y producción; no depende de una base de datos en memoria.
- **Migraciones versionadas:** Flyway crea y actualiza el esquema; Hibernate valida el esquema sin modificarlo.
- **Docker y Docker Compose:** incluye `Dockerfile` y `docker-compose.yml` para construir y ejecutar el backend en un contenedor.
- **Documentación OpenAPI:** Swagger UI permite explorar e invocar los endpoints.

## Estructura del proyecto

```text
src/main/java/com/vetsm/backend/
├── controller/   # Controladores REST de autenticación, mascotas y citas
├── dto/          # Objetos de solicitud y respuesta
├── global/       # Manejo global de excepciones
├── model/        # Entidades y roles
├── repository/   # Repositorios de acceso a datos
└── security/     # Configuración de seguridad, JWT y OpenAPI
```

En la estructura actual, la configuración de seguridad está en `security/`; todavía no existen paquetes independientes `config/` ni `service/`.

## Requisitos y ejecución

**Requisitos:** Java 17 o superior. Para la ejecución con contenedores, Docker y Docker Compose.

### Opción 1: ejecución local

Desde la raíz del repositorio backend, ejecuta:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

### Opción 2: ejecución con Docker Compose

Desde la raíz del backend, ejecuta:

```bash
docker compose up --build -d
```

Compose inicia MySQL y el backend; MySQL persiste sus datos en un volumen Docker. El backend espera a que la base de datos esté lista. Para detener los servicios conserva los datos con `docker compose down`; para borrar también la base local, usa `docker compose down -v`.

### Herramientas

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Perfiles de base de datos

La configuración predeterminada conecta a MySQL local (`localhost:3306`, base `vetsm`, usuario `vetsm`). Para iniciar MySQL junto con el backend en desarrollo, usa Docker Compose. Para producción, activa el perfil `prod` y configura las variables requeridas:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL='jdbc:mysql://<host>:3306/<base_de_datos>?useSSL=true&serverTimezone=UTC'
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<contraseña>
JWT_SECRET=<secreto_aleatorio_largo>
CORS_ALLOWED_ORIGINS=https://nicoherr.github.io
```

`CORS_ALLOWED_ORIGINS` debe contener los orígenes frontend permitidos separados por comas; para este sitio de GitHub Pages usa `https://nicoherr.github.io` (sin la ruta del repositorio). La variable es obligatoria en producción; el valor local predeterminado solo sirve para desarrollo.

Flyway aplica las migraciones de `src/main/resources/db/migration` al iniciar. No se debe cambiar una migración ya aplicada; los cambios de esquema se agregan en una nueva migración.

### Pruebas

Las pruebas de integración también requieren MySQL. Inicia el servicio con `docker compose up -d mysql` y luego ejecuta `./mvnw test` desde la raíz del backend. La suite combina pruebas de integración con Spring y pruebas unitarias de controladores con Mockito. No se usa H2.

## Endpoints de la API

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Registra una cuenta de usuario y devuelve un JWT. |
| `POST` | `/api/auth/login` | Público | Verifica las credenciales y devuelve un JWT. |
| `GET` | `/api/mascotas` | Autenticado | Lista las mascotas del usuario autenticado. |
| `POST` | `/api/mascotas` | Autenticado (sin restricción ADMIN actualmente) | Registra una mascota para el usuario autenticado. |
| `GET` | `/api/citas` | Autenticado | Lista las citas del usuario autenticado. |
| `POST` | `/api/citas` | Autenticado | Agenda una cita para una mascota propia. |

Para los endpoints autenticados, envía el token recibido al iniciar sesión o registrarte como `Authorization: Bearer <token>`. El registro asigna el rol estándar `ROLE_USER`; actualmente no se aplica una restricción exclusiva para `ADMIN` al crear mascotas.

## Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `JWT_SECRET` | Clave local de desarrollo | Clave de firma JWT; obligatoria con perfil `prod`. |
| `JWT_EXPIRATION_MS` | `86400000` ms (24 horas) | Duración de los tokens. |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/vetsm?...` | URL JDBC de MySQL; obligatoria con perfil `prod`. |
| `SPRING_DATASOURCE_USERNAME` | `vetsm` local | Usuario de la base; obligatorio con perfil `prod`. |
| `SPRING_DATASOURCE_PASSWORD` | `vetsm_dev_password` local | Contraseña local; define una segura en producción. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` en desarrollo | Lista de orígenes frontend separados por comas; obligatoria con perfil `prod`. |

> **Nota:** los valores de MySQL y JWT predeterminados son solo para desarrollo local. En producción, usa credenciales y secretos administrados por el entorno de despliegue.
