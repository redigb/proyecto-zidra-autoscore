package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.gpsTelemetria;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("gps_telemetria")
public class GpsTelemetriaEntity {

    @Id
    private Long id;

    @Column("gps_device_id")
    private Long gpsDeviceId;
    // Relación al dispositivo GPS (FK hacia gps_device)
    @Column("fecha_hora")
    private LocalDateTime fechaHora;
    // Fecha y hora de la medición
    @Column("latitud")
    private Double latitud;
    @Column("longitud")
    private Double longitud;
    @Column("speed")
    private Double speed;
    // Velocidad en km/h
    @Column("estado_encendido")
    private Boolean estadoEncendido;
    // TRUE = motor encendido, FALSE = apagado
    @Column("extra_data")
    private JsonNode extraData;
    // Datos adicionales en JSON (ej. temperatura, altitud, etc.)
}
