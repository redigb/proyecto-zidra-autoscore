package com.services_main.main_bk_service.dispositivoGps.application.port.in;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface GpsTelemetriaServicePort {

    Mono<GpsTelemetria> registrarTelemetria(GpsTelemetria telemetria);

    Flux<GpsTelemetria> obtenerTelemetriaPorDispositivo(Long deviceId);

    Mono<GpsTelemetria> obtenerUltimaTelemetria(Long deviceId);

    Flux<GpsTelemetria> obtenerTelemetriaEntreFechas(Long deviceId, LocalDateTime inicio, LocalDateTime fin);

    Mono<Void> eliminarTelemetriaPorDispositivo(Long deviceId);

}
