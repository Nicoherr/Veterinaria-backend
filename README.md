# VetSM — Backend con JWT (Spring Boot)

## Descripción general

VetSM Backend es una API REST para la gestión de una clínica veterinaria. Permite registrar usuarios e iniciar sesión, así como administrar mascotas y citas veterinarias asociadas al usuario autenticado. La aplicación está desarrollada con Spring Boot y utiliza JSON Web Tokens (JWT) para proteger los recursos.

## Características principales

- **Autenticación JWT stateless:** los tokens se envían en el encabezado `Authorization: Bearer <token>`; no se mantienen sesiones en el servidor.
- **Seguridad con Spring Security:** `JwtAuthenticationFilter` valida el token y establece la identidad del usuario en cada solicitud protegida.
- **Manejo global de excepciones:** `GlobalExceptionHandler`, implementado con `@RestControllerAdvice`, normaliza las respuestas de error.
- **Base de datos H2 en memoria:** facilita el desarrollo local; los datos no son persistentes al reiniciar la aplicación.
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

Desde la carpeta `backend/`, ejecuta:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

### Opción 2: ejecución con Docker Compose

Desde la carpeta `backend/`, ejecuta:

```bash
docker-compose up --build -d
```

Compose publica el servicio en el puerto `8080`. Para detenerlo, ejecuta `docker-compose down`.

### Herramientas

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Consola H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Para la consola H2 local, utiliza JDBC URL `jdbc:h2:mem:vetsm`, usuario `sa` y contraseña vacía.

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
| `PORT` | `8080` (puerto habitual de Spring Boot; Compose publica `8080`) | El proyecto no consume actualmente esta variable. Para cambiar el puerto de Spring Boot, utiliza `SERVER_PORT`. |
| `JWT_SECRET` | `VetSM-local-development-secret-change-before-deploy-2026` | Clave de firma JWT. Está conectada a la configuración; reemplázala por un secreto aleatorio y seguro fuera del desarrollo local. |
| `JWT_EXPIRATION_MS` | `86400000` ms (24 horas) | Duración de los tokens. Ese valor está configurado en la aplicación, pero la variable no está conectada actualmente; el valor se puede establecer con `APP_JWT_EXPIRATION_MS`. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Orígenes permitidos configurados para CORS. La variable todavía no está conectada; los orígenes están definidos en `SecurityConfig`. |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:vetsm;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` | URL JDBC de la base de datos. Spring Boot permite sobrescribir esta propiedad con la variable de entorno indicada. |

> **Nota:** la configuración predeterminada de JWT es solo para desarrollo. La base H2 es volátil; al reiniciar la aplicación, se pierden los datos almacenados.
