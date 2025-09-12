package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.gpsTelemetria;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.GpsTelemetria;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.gpsTelemetria.GpsTelemetriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GpsTelemetriaPersistenceMapper {

    @Mapping(source = "gpsDevice.id", target = "gpsDeviceId")
    @Mapping(source = "extraData", target = "extraData")
    GpsTelemetriaEntity toEntity(GpsTelemetria domain);

    @Mapping(source = "gpsDeviceId", target = "gpsDevice.id")
    @Mapping(source = "extraData", target = "extraData")
    GpsTelemetria toDomain(GpsTelemetriaEntity entity);

    //List<GpsTelemetria> toDomainList (List<GpsTelemetriaEntity>  gpsTelemetriaEntityList);
}
