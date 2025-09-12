package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.artefactoGpsLink;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink.ArtefactoGpsLinkRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink.ArtefactoGpsLinkResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArtefactoGpsLinkRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaAsignacion", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "fechaDesvinculacion", ignore = true)
    @Mapping(target = "estado", constant = "ACTIVO") // MapStruct convierte "ACTIVO" → EstadoAsignacion.ACTIVO
    @Mapping(target = "iotiGps.id", source = "gpsDeviceId")
    ArtefactoGpsLink toDomain(ArtefactoGpsLinkRequest request);

    @Mapping(target = "gpsDeviceId", source = "iotiGps.id")
    @Mapping(target = "estado", expression = "java(link.getEstado().name())")
    ArtefactoGpsLinkResponse toResponse(ArtefactoGpsLink link);
}
