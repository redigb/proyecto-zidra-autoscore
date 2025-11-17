package com.services_main.main_bk_service.evaluador.repository;

import com.services_main.main_bk_service.evaluador.entity.InterpretacionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface InterpretacionRepository extends ReactiveCrudRepository<InterpretacionEntity, Long> {
    Mono<InterpretacionEntity> findByScoringId(Long scoringId);
}
