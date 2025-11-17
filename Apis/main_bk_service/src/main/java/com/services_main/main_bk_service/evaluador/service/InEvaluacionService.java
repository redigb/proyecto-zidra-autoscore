package com.services_main.main_bk_service.evaluador.service;

import com.services_main.main_bk_service.evaluador.entity.InterpretacionEntity;
import com.services_main.main_bk_service.evaluador.entity.PreClienteEntity;
import com.services_main.main_bk_service.evaluador.entity.ScoringEntity;
import com.services_main.main_bk_service.evaluador.entity.SolicitudCreditoEntity;
import com.services_main.main_bk_service.evaluador.request.EvaluacionRequest;
import com.services_main.main_bk_service.evaluador.request.PreClienteRequest;
import com.services_main.main_bk_service.evaluador.request.SolicitudCreditoRequest;
import com.services_main.main_bk_service.evaluador.response.EvaluacionResponse;
import com.services_main.main_bk_service.evaluador.response.ModeloActivoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface InEvaluacionService {

    Mono<EvaluacionResponse> evaluarSolicitud(Long id, EvaluacionRequest req);

   Mono<PreClienteEntity> crearPreCliente(PreClienteRequest req);

   Mono<SolicitudCreditoEntity> crearSolicitud(SolicitudCreditoRequest req);

    Mono<PreClienteEntity> obtenerPreCliente(Long id);
    Flux<PreClienteEntity> listarPreClientes();

    Mono<SolicitudCreditoEntity> obtenerSolicitud(Long id);
    Flux<SolicitudCreditoEntity> listarSolicitudesPorPreCliente(Long preClienteId);

    Mono<ScoringEntity> obtenerScoringPorSolicitud(Long solicitudId);
    Mono<InterpretacionEntity> obtenerInterpretacion(Long scoringId);
}
