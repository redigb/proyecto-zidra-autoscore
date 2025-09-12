package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.out;

import com.services_main.main_bk_service.dispositivoGps.application.port.out.GpsTelemetriaPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.gpsTelemetria.GpsTelemetriaPersistenceMapper;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.gpsTelemetria.GpsTelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GpsTelemetriaPersistenceAdapter implements GpsTelemetriaPersistencePort {

    private final GpsTelemetriaRepository gpsTelemetriaRepository;
    private final GpsTelemetriaPersistenceMapper mapper;

    @Override
    public Mono<GpsTelemetria> saveTelemetry(GpsTelemetria telemetry) {
        return gpsTelemetriaRepository.save(mapper.toEntity(telemetry))
                .map(mapper::toDomain);
    }

    @Override
    public Flux<GpsTelemetria> findTelemetryByDevice(Long deviceId) {
        return gpsTelemetriaRepository.findByGpsDeviceId(deviceId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<GpsTelemetria> findLastTelemetryByDevice(Long deviceId) {
        return gpsTelemetriaRepository.findLastByGpsDeviceId(deviceId)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<GpsTelemetria> findTelemetryBetweenDates(Long deviceId, LocalDateTime start, LocalDateTime end) {
        return gpsTelemetriaRepository.findBetweenDates(deviceId, start, end)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> deleteTelemetryByDevice(Long deviceId) {
        return gpsTelemetriaRepository.deleteOldTelemetryByDeviceId(deviceId);
    }
}
