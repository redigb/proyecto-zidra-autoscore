package com.services_main.main_bk_service.dispositivoGps.application.port.out;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IotiGpsPersistencePort {

    Mono<IotiGps> saveDispositivo(IotiGps device);

    Mono<IotiGps> findDispositivoById(Long id);

    Mono<IotiGps> findDispositivoByImei(String imei);

    Flux<IotiGps> findAllDispositivos();

    Mono<IotiGps> updateDispositivo(IotiGps device);

    Mono<Void> deleteDispositivo(Long id);

    Mono<IotiGps> findByDeviceCodeOrImei(String deviceCode, String imei);
}
