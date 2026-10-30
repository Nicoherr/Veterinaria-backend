# VetSM — Backend con JWT (Spring Boot)

## Descripción general

VetSM Backend es una API REST para la gestión de una clínica veterinaria. Permite registrar usuarios e iniciar sesión, así como administrar mascotas y citas veterinarias asociadas al usuario autenticado. La aplicación está desarrollada con Spring Boot y utiliza JSON Web Tokens (JWT) para proteger los recursos.

## Características principales

- **Autenticación JWT stateless:** los tokens se envían en el encabezado `Authorization: Bearer <token>`; no se mantienen sesiones en el servidor.
- **Seguridad con Spring Security:** `JwtAuthenticationFilter` valida el token y establece la identidad del usuario en cada solicitud protegida.
- **Manejo global de excepciones:** `GlobalExceptionHandler`, implementado con `@RestControllerAdvice`, normaliza las respuestas de error.
- **Base de datos:** H2 en memoria para desarrollo y pruebas; MySQL para producción.
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

### Perfiles de base de datos

La configuración predeterminada utiliza H2 en memoria. Para producción, activa el perfil `prod` y configura las variables requeridas:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/<base_de_datos>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<contraseña>
JWT_SECRET=<secreto_aleatorio_largo>
```

Flyway aplica las migraciones de `src/main/resources/db/migration` al iniciar. No se debe cambiar una migración ya aplicada; los cambios de esquema se agregan en una nueva migración.

### Pruebas

Ejecuta `./mvnw test` desde la raíz del backend. La suite combina pruebas de integración con Spring y pruebas unitarias de controladores con Mockito.

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
| `SPRING_DATASOURCE_URL` | URL H2 local | URL JDBC; obligatoria con perfil `prod`. |
| `SPRING_DATASOURCE_USERNAME` | `sa` local | Usuario de la base; obligatorio con perfil `prod`. |
| `SPRING_DATASOURCE_PASSWORD` | Vacía local | Contraseña de la base; obligatoria con perfil `prod`. |

> **Nota:** la configuración predeterminada de JWT y H2 es solo para desarrollo. La base H2 es volátil; al reiniciar la aplicación, se pierden los datos almacenados. En producción, utiliza credenciales y secretos administrados por el entorno de despliegue.
