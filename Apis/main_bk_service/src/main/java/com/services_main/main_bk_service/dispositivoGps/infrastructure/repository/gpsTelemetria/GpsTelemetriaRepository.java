package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.gpsTelemetria;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface GpsTelemetriaRepository extends ReactiveCrudRepository<GpsTelemetriaEntity, Long> {

    // Obtener todo el historial de telemetría de un dispositivo
    Flux<GpsTelemetriaEntity> findByGpsDeviceId(Long gpsDeviceId);

    // Obtener la última telemetría registrada para un dispositivo
    @Query("SELECT * FROM gps_telemetria WHERE gps_device_id = :deviceId ORDER BY fecha_hora DESC LIMIT 1")
    Mono<GpsTelemetriaEntity> findLastByGpsDeviceId(Long deviceId);

    // Obtener telemetría en un rango de fechas
    @Query("SELECT * FROM gps_telemetria " +
            "WHERE gps_device_id = :deviceId " +
            "AND fecha_hora BETWEEN :start AND :end " +
            "ORDER BY fecha_hora ASC")
    Flux<GpsTelemetriaEntity> findBetweenDates(Long deviceId, LocalDateTime start, LocalDateTime end);

    // Eliminar telemetría de un dispositivo
    @Modifying
    @Query("""
    DELETE FROM gps_telemetria g
    WHERE g.gps_device_id = :deviceId
      AND g.id NOT IN (
        SELECT id
        FROM gps_telemetria
        WHERE gps_device_id = :deviceId
        ORDER BY fecha_hora DESC
        LIMIT 1
      )
    """)
    Mono<Long> deleteOldTelemetryByDeviceId(Long deviceId);


}
