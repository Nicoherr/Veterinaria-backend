CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE TABLE mascotas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255),
    tipo VARCHAR(255),
    raza VARCHAR(255),
    edad INTEGER,
    microchip VARCHAR(255),
    usuario_id BIGINT NOT NULL,
    CONSTRAINT pk_mascotas PRIMARY KEY (id),
    CONSTRAINT fk_mascotas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE citas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE,
    hora TIME,
    motivo VARCHAR(255),
    mascota_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT pk_citas PRIMARY KEY (id),
    CONSTRAINT fk_citas_mascota FOREIGN KEY (mascota_id) REFERENCES mascotas (id),
    CONSTRAINT fk_citas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
