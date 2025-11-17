package com.services_main.main_bk_service.evaluador.repository;



import com.services_main.main_bk_service.evaluador.entity.PreClienteEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreClienteRepository extends ReactiveCrudRepository<PreClienteEntity, Long> {}
