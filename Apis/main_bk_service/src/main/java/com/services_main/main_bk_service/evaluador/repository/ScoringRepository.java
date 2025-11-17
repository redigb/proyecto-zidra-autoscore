package com.services_main.main_bk_service.evaluador.repository;

import com.services_main.main_bk_service.evaluador.entity.ScoringEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ScoringRepository extends ReactiveCrudRepository<ScoringEntity, Long> {
    Mono<ScoringEntity> findBySolicitudId(Long solicitudId);
}
