package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.artefactoGpsLink;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArtefactoGpsLinkRepository extends ReactiveCrudRepository<ArtefactoGpsLinkEntity, Long> {

    // Buscar vínculo por el ID del artefacto en PocketBase
    Flux<ArtefactoGpsLinkEntity> findByPocketbaseArtefactoId(String pocketbaseArtefactoId);

    // Buscar vínculo activo de un GPS en particular
    @Query("SELECT * FROM artefacto_gps_link " +
            "WHERE gps_device_id = :deviceId " +
            "AND estado = 'ACTIVO' " +
            "LIMIT 1")
    Mono<ArtefactoGpsLinkEntity> findActiveByGpsDeviceId(Long deviceId);

    // Listar todos los vínculos de un GPS (histórico: activo + retirado)
    Flux<ArtefactoGpsLinkEntity> findByGpsDeviceId(Long deviceId);
}
