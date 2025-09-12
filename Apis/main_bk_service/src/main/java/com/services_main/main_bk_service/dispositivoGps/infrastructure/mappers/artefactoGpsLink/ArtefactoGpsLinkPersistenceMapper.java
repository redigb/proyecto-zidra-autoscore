package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.artefactoGpsLink;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.artefactoGpsLink.ArtefactoGpsLinkEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArtefactoGpsLinkPersistenceMapper {

    @Mapping(source = "gpsDeviceId", target = "iotiGps.id")
    ArtefactoGpsLink toDomain(ArtefactoGpsLinkEntity entity);

    @Mapping(source = "iotiGps.id", target = "gpsDeviceId")
    ArtefactoGpsLinkEntity toEntity(ArtefactoGpsLink domain);
}
