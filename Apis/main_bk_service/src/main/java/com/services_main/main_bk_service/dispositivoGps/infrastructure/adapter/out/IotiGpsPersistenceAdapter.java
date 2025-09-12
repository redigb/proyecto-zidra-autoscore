package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.out;


import com.services_main.main_bk_service.dispositivoGps.application.port.out.IotiGpsPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.iotiGps.IotiGpsPersistenceMapper;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.iotiGps.IotiGpsEntity;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.iotiGps.IotiGpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class IotiGpsPersistenceAdapter implements IotiGpsPersistencePort {

    private final IotiGpsRepository iotiGpsRepository;
    private final IotiGpsPersistenceMapper mapper;

    @Override
    public Mono<IotiGps> saveDispositivo(IotiGps device) {
        return iotiGpsRepository.save(mapper.toEntity(device))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<IotiGps> findDispositivoById(Long id) {
        return iotiGpsRepository.findById(id)
                .map(mapper::toDomain); // sin manejo de error aquí
    }

    @Override
    public Mono<IotiGps> findDispositivoByImei(String imei) {
        return iotiGpsRepository.findByImei(imei)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<IotiGps> findAllDispositivos() {
        return iotiGpsRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<IotiGps> updateDispositivo(IotiGps device) {
        return iotiGpsRepository.findById(device.getId())
                .flatMap(existing -> {
                    IotiGpsEntity updated = mapper.toEntity(device);
                    updated.setId(existing.getId()); // mantener ID
                    return iotiGpsRepository.save(updated);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteDispositivo(Long id) {
        return iotiGpsRepository.deleteById(id);
    }

    @Override
    public Mono<IotiGps> findByDeviceCodeOrImei(String deviceCode, String imei) {
        return iotiGpsRepository.findByDeviceCodeOrImei(deviceCode, imei)
                .map(mapper::toDomain);
    }
}
