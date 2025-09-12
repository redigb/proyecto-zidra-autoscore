package com.services_main.main_bk_service.dispositivoGps.application.service;
import com.services_main.main_bk_service.dispositivoGps.application.port.in.GpsTelemetriaServicePort;
import com.services_main.main_bk_service.dispositivoGps.application.port.out.GpsTelemetriaPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import com.services_main.main_bk_service.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GpsTelemetriaService implements GpsTelemetriaServicePort {

    private final GpsTelemetriaPersistencePort gpsTelemetriaPersistencePort;

    @Override
    public Mono<GpsTelemetria> registrarTelemetria(GpsTelemetria nueva) {
        // 1) caso: no hay última -> guardar primera
        return gpsTelemetriaPersistencePort.findLastTelemetryByDevice(nueva.getGpsDevice().getId())
                .switchIfEmpty(Mono.defer(() -> {
                    //System.out.println("🆕 No había telemetría previa, guardando primera entrada.");
                    return gpsTelemetriaPersistencePort.saveTelemetry(nueva);
                }))
                // 2) hay última -> aplicar regla de “cooldown 1 min” + “debe haber cambio”
                .flatMap(ultima -> {
                    // tolerancias para evitar ruido
                    boolean cambioLat = Math.abs(ultima.getLatitud()  - nueva.getLatitud())  > 0.00001;
                    boolean cambioLon = Math.abs(ultima.getLongitud() - nueva.getLongitud()) > 0.00001;

                    boolean cambioSpeed = !ultima.getSpeed().equals(nueva.getSpeed());
                    boolean huboCambio = cambioLat || cambioLon || cambioSpeed;
                    boolean pasoUnMinuto = !nueva.getFechaHora().isBefore(ultima.getFechaHora().plusMinutes(1));

                    if (pasoUnMinuto && huboCambio) {
                        //System.out.println("✅ Guardando: pasó ≥1 min y hubo cambio.");
                        return gpsTelemetriaPersistencePort.saveTelemetry(nueva);
                    } else if (!pasoUnMinuto) {
                       // System.out.println("⏸️ Descartada: aún no pasa 1 min desde la última.");
                        return Mono.empty();
                    } else { // pasoUnMinuto == true pero NO hubo cambio
                        //System.out.println("⏸️ Descartada: pasó ≥1 min pero NO hubo cambio.");
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Flux<GpsTelemetria> obtenerTelemetriaPorDispositivo(Long deviceId) {
        return gpsTelemetriaPersistencePort.findTelemetryByDevice(deviceId)
                .switchIfEmpty(Flux.error(new NotFoundException("No se encontró telemetría para el dispositivo con id: " + deviceId)));
    }

    @Override
    public Mono<GpsTelemetria> obtenerUltimaTelemetria(Long deviceId) {
        return gpsTelemetriaPersistencePort.findLastTelemetryByDevice(deviceId)
                .switchIfEmpty(Mono.error(new NotFoundException("No se encontró telemetría reciente para el dispositivo con id: " + deviceId)));
    }

    @Override
    public Flux<GpsTelemetria> obtenerTelemetriaEntreFechas(Long deviceId, LocalDateTime inicio, LocalDateTime fin) {
        return gpsTelemetriaPersistencePort.findTelemetryBetweenDates(deviceId, inicio, fin)
                .switchIfEmpty(Flux.error(new NotFoundException(
                        "No se encontró telemetría entre las fechas especificadas para el dispositivo con id: " + deviceId
                )));
    }

    @Override
    public Mono<Void> eliminarTelemetriaPorDispositivo(Long deviceId) {
        return gpsTelemetriaPersistencePort.deleteTelemetryByDevice(deviceId)
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.error(new NotFoundException(
                                "No existe telemetría para el dispositivo con id: " + deviceId));
                    }
                    return Mono.empty(); // éxito
                });
    }
}
