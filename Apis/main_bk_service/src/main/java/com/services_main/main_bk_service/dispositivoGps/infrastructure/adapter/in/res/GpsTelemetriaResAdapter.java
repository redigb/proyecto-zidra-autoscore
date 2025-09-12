package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.res;

import com.services_main.main_bk_service.dispositivoGps.application.port.in.GpsTelemetriaServicePort;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaResponse;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.gpsTelemetria.GpsTelemetriaRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${api.prefix}/gps-telemetria")
public class GpsTelemetriaResAdapter {

    private final GpsTelemetriaServicePort gpsTelemetriaServicePort;
    private final GpsTelemetriaRestMapper mapper;

   /* @PostMapping
    public Mono<GpsTelemetriaResponse> registrar(@RequestBody GpsTelemetriaRequest request) {
        return gpsTelemetriaServicePort.registrarTelemetria(mapper.toDomain(request))
                .map(mapper::toResponse);
    }*/

    @GetMapping("/device/{deviceId}")
    public Flux<GpsTelemetriaResponse> obtenerPorDispositivo(@PathVariable Long deviceId) {
        return gpsTelemetriaServicePort.obtenerTelemetriaPorDispositivo(deviceId)
                .map(mapper::toResponse);
    }

    @GetMapping("/device/{deviceId}/last")
    public Mono<GpsTelemetriaResponse> obtenerUltima(@PathVariable Long deviceId) {
        return gpsTelemetriaServicePort.obtenerUltimaTelemetria(deviceId)
                .map(mapper::toResponse);
    }

    @GetMapping("/device/{deviceId}/between")
    public Flux<GpsTelemetriaResponse> obtenerEntreFechas(@PathVariable Long deviceId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        return gpsTelemetriaServicePort.obtenerTelemetriaEntreFechas(deviceId, inicio, fin)
                .map(mapper::toResponse);
    }

    @DeleteMapping("/device/{deviceId}")
    public Mono<Void> eliminarPorDispositivo(@PathVariable Long deviceId) {
        return gpsTelemetriaServicePort.eliminarTelemetriaPorDispositivo(deviceId);
    }

}
