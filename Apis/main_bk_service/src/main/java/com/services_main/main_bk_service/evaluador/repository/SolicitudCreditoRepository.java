package com.services_main.main_bk_service.evaluador.repository;

import com.services_main.main_bk_service.evaluador.entity.SolicitudCreditoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SolicitudCreditoRepository extends ReactiveCrudRepository<SolicitudCreditoEntity, Long> {
    Flux<SolicitudCreditoEntity> findByPreClienteId(Long preClienteId);
}