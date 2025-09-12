package com.services_main.main_bk_service.dispositivoGps.application.service;
import com.services_main.main_bk_service.dispositivoGps.application.port.in.ArtefactoLinkServicePort;
import com.services_main.main_bk_service.dispositivoGps.application.port.out.ArtefactoGpsLinkPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.dispositivoGps.domain.valueObject.EstadoAsignacion;
import com.services_main.main_bk_service.shared.exception.ConflictException;
import com.services_main.main_bk_service.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArtefactoLinkService implements ArtefactoLinkServicePort {

    private final ArtefactoGpsLinkPersistencePort gpsLinkPersistencePort;

    @Override
    public Mono<ArtefactoGpsLink> vincularGpsAArtefacto(String artefactoIdPocketbase, Long gpsDeviceId) {
        // Primero validar que no exista un vínculo activo con este GPS
        return gpsLinkPersistencePort.findActiveByDeviceId(gpsDeviceId)
                .flatMap(existing -> Mono.<ArtefactoGpsLink>error(
                        new ConflictException("El dispositivo GPS con id " + gpsDeviceId +
                                " ya está vinculado a un artefacto")))
                .switchIfEmpty(
                        gpsLinkPersistencePort.saveLink(buildNewLink(artefactoIdPocketbase, gpsDeviceId))
                );
    }

    @Override
    public Flux<ArtefactoGpsLink> obtenerVinculoPorArtefacto(String artefactoIdPocketbase) {
        return gpsLinkPersistencePort.findByPocketbaseArtefactoId(artefactoIdPocketbase)
                .switchIfEmpty(Flux.error(new NotFoundException(
                        "No se encontró vínculo para el artefacto con id: " + artefactoIdPocketbase)));
    }

    @Override
    public Mono<ArtefactoGpsLink> obtenerVinculoActivoPorDispositivo(Long gpsDeviceId) {
        return gpsLinkPersistencePort.findActiveByDeviceId(gpsDeviceId)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "No se encontró un vínculo activo para el GPS con id: " + gpsDeviceId)));
    }

    @Override
    public Mono<ResponseEntity<Flux<ArtefactoGpsLink>>> listarVinculos() {
        return gpsLinkPersistencePort.findAllLinks()
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.just(ResponseEntity.noContent().build()); // 204
                    }
                    return Mono.just(ResponseEntity.ok(Flux.fromIterable(list))); // 200 + lista
                });
    }

    @Override
    public Mono<ArtefactoGpsLink> desvincularGps(Long linkId) {
        return gpsLinkPersistencePort.unlinkDevice(linkId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("No se pudo desvincular el GPS, no existe vínculo con id: " + linkId)
                ));

        // Sinn un dispositivo es desviculado se debe quitar d ela lista de el broker o de sus accesos
    }


     // Queda añadir .. elimianr vinculaciones por 30 dias osea mes en registro practicamente un cron aqui


    // Helper privado
    private ArtefactoGpsLink buildNewLink(String artefactoIdPocketbase, Long gpsDeviceId) {
        ArtefactoGpsLink link = new ArtefactoGpsLink();
        link.setPocketbaseArtefactoId(artefactoIdPocketbase);

        IotiGps gps = new IotiGps();
        gps.setId(gpsDeviceId);
        link.setIotiGps(gps);

        link.setFechaAsignacion(LocalDateTime.now());
        link.setEstado(EstadoAsignacion.ACTIVO);
        return link;
    }
}
