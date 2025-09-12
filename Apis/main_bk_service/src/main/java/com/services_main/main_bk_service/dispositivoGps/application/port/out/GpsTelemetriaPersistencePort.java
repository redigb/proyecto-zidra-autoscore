package com.services_main.main_bk_service.dispositivoGps.application.port.out;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface GpsTelemetriaPersistencePort {

    Mono<GpsTelemetria> saveTelemetry(GpsTelemetria telemetry);

    Flux<GpsTelemetria> findTelemetryByDevice(Long deviceId);

    Mono<GpsTelemetria> findLastTelemetryByDevice(Long deviceId);

    Flux<GpsTelemetria> findTelemetryBetweenDates(Long deviceId, LocalDateTime start, LocalDateTime end);

    Mono<Long> deleteTelemetryByDevice(Long deviceId);
}
