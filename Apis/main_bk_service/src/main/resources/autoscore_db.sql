-- ==========================================================
-- 0. Crear Base de Datos (seguro, no rompe migraciones)
-- ==========================================================
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_database WHERE datname = 'scoring_risk'
    ) THEN
        CREATE DATABASE scoring_risk;
    END IF;
END
$$;

-- IMPORTANTE:
-- Ejecutar lo siguiente solo si estás en psql:
-- \c scoring_risk;


-- ==========================================================
-- 1. Tabla: pre_cliente
-- ==========================================================
CREATE TABLE IF NOT EXISTS pre_cliente (
    id BIGSERIAL PRIMARY KEY,
    numero_dni VARCHAR(20),
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    direccion TEXT,
    referencia_vivienda TEXT,
    distrito VARCHAR(100),
    provincia VARCHAR(100),
    departamento VARCHAR(100),
    numero_celular VARCHAR(20),
    numero_celular_secundario VARCHAR(20),
    correo_electronico VARCHAR(150),
    created TIMESTAMP DEFAULT NOW(),
    updated TIMESTAMP
);

-- ==========================================================
-- 2. Tabla: solicitud_credito
-- ==========================================================
CREATE TABLE IF NOT EXISTS solicitud_credito (
    id BIGSERIAL PRIMARY KEY,

    pre_cliente_id BIGINT NOT NULL,
    monto_total NUMERIC(12,2),
    monto_inicial NUMERIC(12,2),
    numero_cuotas INTEGER,
    frecuencia_pago VARCHAR(20),
    interes NUMERIC(5,2),
    monto_cuota NUMERIC(12,2),
    total_pagar NUMERIC(12,2),

    estado_predicente VARCHAR(20),
    fecha_solicitud TIMESTAMP DEFAULT NOW(),
    usuario_asesor_id BIGINT,
    updated TIMESTAMP,

    CONSTRAINT fk_solicitud_pre_cliente
        FOREIGN KEY (pre_cliente_id)
            REFERENCES pre_cliente(id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 3. Tabla: scoring
-- ==========================================================
CREATE TABLE IF NOT EXISTS scoring (
    id BIGSERIAL PRIMARY KEY,

    solicitud_id BIGINT NOT NULL,
    riesgo NUMERIC(5,4),
    modelo_version VARCHAR(50),
    variables_json TEXT,
    fecha_evaluacion TIMESTAMP DEFAULT NOW(),
    created TIMESTAMP DEFAULT NOW(),
    updated TIMESTAMP,

    CONSTRAINT fk_scoring_solicitud
        FOREIGN KEY (solicitud_id)
            REFERENCES solicitud_credito(id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 4. Tabla: interpretacion
-- ==========================================================
CREATE TABLE IF NOT EXISTS interpretacion (
    id BIGSERIAL PRIMARY KEY,

    scoring_id BIGINT NOT NULL,
    resumen TEXT,
    fecha_generacion TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_interpretacion_scoring
        FOREIGN KEY (scoring_id)
            REFERENCES scoring(id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 5. IoT GPS - Tabla: ioti_gps
-- ==========================================================
CREATE TABLE IF NOT EXISTS ioti_gps (
    id BIGSERIAL PRIMARY KEY,

    device_code VARCHAR(100),
    imei VARCHAR(50),
    modelo VARCHAR(100),
    version_firmware VARCHAR(100),

    status VARCHAR(20),  -- ONLINE / OFFLINE

    sim_numero_telefono VARCHAR(50),
    sim_operador VARCHAR(50),
    sim_plan VARCHAR(100),

    mqtt_username VARCHAR(100),
    mqtt_password VARCHAR(100),

    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================================
-- 6. IoT GPS - Tabla: gps_telemetria
-- ==========================================================
CREATE TABLE IF NOT EXISTS gps_telemetria (
    id BIGSERIAL PRIMARY KEY,

    gps_device_id BIGINT NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,

    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    speed DOUBLE PRECISION,

    estado_encendido BOOLEAN,

    extra_data JSONB,

    CONSTRAINT fk_telemetria_gps
        FOREIGN KEY (gps_device_id)
            REFERENCES ioti_gps(id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 7. IoT GPS - Tabla: artefacto_gps_link
-- ==========================================================
CREATE TABLE IF NOT EXISTS artefacto_gps_link (
    id BIGSERIAL PRIMARY KEY,

    pocketbase_artefacto_id VARCHAR(100),
    gps_device_id BIGINT NOT NULL,

    fecha_asignacion TIMESTAMP,
    fecha_desvinculacion TIMESTAMP,

    estado VARCHAR(20),  -- ACTIVO / RETIRADO

    CONSTRAINT fk_artefacto_gps
        FOREIGN KEY (gps_device_id)
            REFERENCES ioti_gps(id)
            ON DELETE CASCADE
);

-- ==========================================================
-- 8. Índices generales
-- ==========================================================
CREATE INDEX IF NOT EXISTS idx_solicitud_pre_cliente
    ON solicitud_credito(pre_cliente_id);

CREATE INDEX IF NOT EXISTS idx_scoring_solicitud
    ON scoring(solicitud_id);

CREATE INDEX IF NOT EXISTS idx_interpretacion_scoring
    ON interpretacion(scoring_id);

CREATE INDEX IF NOT EXISTS idx_telemetria_gps
    ON gps_telemetria(gps_device_id);

CREATE INDEX IF NOT EXISTS idx_gps_imei
    ON ioti_gps(imei);

CREATE INDEX IF NOT EXISTS idx_gps_device_code
    ON ioti_gps(device_code);

CREATE INDEX IF NOT EXISTS idx_artefacto_gps
    ON artefacto_gps_link(gps_device_id);
