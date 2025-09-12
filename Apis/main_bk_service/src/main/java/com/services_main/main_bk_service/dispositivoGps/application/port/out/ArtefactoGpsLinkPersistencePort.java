package com.services_main.main_bk_service.dispositivoGps.application.port.out;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArtefactoGpsLinkPersistencePort {

    Mono<ArtefactoGpsLink> saveLink(ArtefactoGpsLink link);

    Mono<ArtefactoGpsLink> findById(Long id);

    Flux<ArtefactoGpsLink> findByPocketbaseArtefactoId(String artefactoId);

    Mono<ArtefactoGpsLink> findActiveByDeviceId(Long deviceId);

    Flux<ArtefactoGpsLink> findAllLinks();

    Mono<Void> deleteById(Long id);

    Mono<ArtefactoGpsLink> unlinkDevice(Long id); // cambia estado a RETIRADO
}
