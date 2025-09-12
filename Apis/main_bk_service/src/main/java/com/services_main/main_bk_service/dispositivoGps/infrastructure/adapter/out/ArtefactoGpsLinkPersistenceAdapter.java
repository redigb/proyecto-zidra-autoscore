package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.out;

import com.services_main.main_bk_service.dispositivoGps.application.port.out.ArtefactoGpsLinkPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.ArtefactoGpsLink;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.artefactoGpsLink.ArtefactoGpsLinkPersistenceMapper;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.artefactoGpsLink.ArtefactoGpsLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class ArtefactoGpsLinkPersistenceAdapter implements ArtefactoGpsLinkPersistencePort {

    private final ArtefactoGpsLinkRepository gpsLinkRepository;
    private final ArtefactoGpsLinkPersistenceMapper mapper;

    @Override
    public Mono<ArtefactoGpsLink> saveLink(ArtefactoGpsLink link) {
        return gpsLinkRepository.save(mapper.toEntity(link))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ArtefactoGpsLink> findById(Long id) {
        return gpsLinkRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<ArtefactoGpsLink> findByPocketbaseArtefactoId(String artefactoId) {
        return gpsLinkRepository.findByPocketbaseArtefactoId(artefactoId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ArtefactoGpsLink> findActiveByDeviceId(Long deviceId) {
        return gpsLinkRepository.findActiveByGpsDeviceId(deviceId)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<ArtefactoGpsLink> findAllLinks() {
        return gpsLinkRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return gpsLinkRepository.deleteById(id);
    }

    @Override
    public Mono<ArtefactoGpsLink> unlinkDevice(Long id) {
        return gpsLinkRepository.findById(id)
                .flatMap(entity -> {
                    entity.setEstado("RETIRADO");
                    entity.setFechaDesvinculacion(LocalDateTime.now());
                    return gpsLinkRepository.save(entity);
                })
                .map(mapper::toDomain);
    }
}
