package com.services_main.main_bk_service.dispositivoGps.application.port.in;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArtefactoLinkServicePort {

    Mono<ArtefactoGpsLink> vincularGpsAArtefacto(String artefactoIdPocketbase, Long gpsDeviceId);

    Flux<ArtefactoGpsLink> obtenerVinculoPorArtefacto(String artefactoIdPocketbase);

    Mono<ArtefactoGpsLink> obtenerVinculoActivoPorDispositivo(Long gpsDeviceId);

    Mono<ResponseEntity<Flux<ArtefactoGpsLink>>> listarVinculos();

    Mono<ArtefactoGpsLink> desvincularGps(Long linkId);

}
