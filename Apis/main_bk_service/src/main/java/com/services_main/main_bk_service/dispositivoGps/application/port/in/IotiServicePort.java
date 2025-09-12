package com.services_main.main_bk_service.dispositivoGps.application.port.in;

import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IotiServicePort {

    Mono<IotiGps> registrarDispositivo(IotiGps gps);

    Mono<IotiGps> obtenerDispositivoPorId(Long id);

    Mono<IotiGps> obtenerDispositivoPorImei(String imei);

    Flux<IotiGps> listarDispositivos();

    Mono<IotiGps> actualizarDispositivo(IotiGps gps);

    Mono<Void> eliminarDispositivo(Long id);

}
