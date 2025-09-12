package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.iotiGps;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IotiGpsRepository extends ReactiveCrudRepository<IotiGpsEntity, Long> {

    // Buscar un dispositivo por su IMEI
    Mono<IotiGpsEntity> findByImei(String imei);

    // Buscar un dispositivo por su código interno
    Mono<IotiGpsEntity> findByDeviceCode(String deviceCode);

    // Listar dispositivos según estado (ONLINE / OFFLINE)
    Flux<IotiGpsEntity> findByStatus(String status);

    // Validador
    @Query("SELECT * FROM ioti_gps WHERE device_code = :deviceCode OR imei = :imei LIMIT 1")
    Mono<IotiGpsEntity> findByDeviceCodeOrImei(String deviceCode, String imei);
}