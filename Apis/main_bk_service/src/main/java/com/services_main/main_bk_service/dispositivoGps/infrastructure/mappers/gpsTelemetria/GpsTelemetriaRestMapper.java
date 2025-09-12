package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.gpsTelemetria;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria.GpsTelemetriaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GpsTelemetriaRestMapper {

    // request -> dominio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaHora", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "gpsDevice.id", source = "gpsDeviceId") // setea solo el id del GPS
    GpsTelemetria toDomain(GpsTelemetriaRequest request);

    // dominio -> response
    @Mapping(target = "gpsDeviceId", source = "gpsDevice.id")
    GpsTelemetriaResponse toResponse(GpsTelemetria telemetria);
}
