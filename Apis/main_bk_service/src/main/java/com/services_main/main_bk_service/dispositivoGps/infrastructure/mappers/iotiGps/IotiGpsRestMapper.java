package com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.iotiGps;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps.IotiGpsRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps.IotiGpsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IotiGpsRestMapper {

    IotiGps toDomain(IotiGpsRequest request);

    IotiGpsResponse toResponse(IotiGps gps);
}
