CREATE DATABASE IF NOT EXISTS filey
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE filey;

CREATE TABLE IF NOT EXISTS editorial (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS stand (
    id INT PRIMARY KEY,
    ancho DECIMAL(12,2) NOT NULL,
    largo DECIMAL(12,2) NOT NULL,
    precio_m2 DECIMAL(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS reserva (
    id INT PRIMARY KEY,
    id_editorial INT NOT NULL,
    id_stand INT NOT NULL,
    fecha_creacion DATE NOT NULL,
    vencimiento DATE NOT NULL,
    limite_liquidacion DATE NOT NULL,
    vencida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reserva_editorial
        FOREIGN KEY (id_editorial) REFERENCES editorial(id),
    CONSTRAINT fk_reserva_stand
        FOREIGN KEY (id_stand) REFERENCES stand(id)
);

CREATE TABLE IF NOT EXISTS pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    fecha DATE NOT NULL,
    CONSTRAINT fk_pago_reserva
        FOREIGN KEY (id_reserva) REFERENCES reserva(id)
        ON DELETE CASCADE
);
