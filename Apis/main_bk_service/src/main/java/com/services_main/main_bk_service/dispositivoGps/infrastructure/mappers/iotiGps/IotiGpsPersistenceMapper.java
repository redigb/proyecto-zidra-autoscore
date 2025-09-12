package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.iotiGps;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.iotiGps.IotiGpsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IotiGpsPersistenceMapper {

    // De entidad (BD) → dominio
    IotiGps toDomain(IotiGpsEntity entity);
    // De dominio → entidad (BD)
    IotiGpsEntity toEntity(IotiGps domain);
    // Mapear listas de entidades a listas de dominio
    //List<IotiGps> toDomainList(List<IotiGpsEntity> entities);

}
