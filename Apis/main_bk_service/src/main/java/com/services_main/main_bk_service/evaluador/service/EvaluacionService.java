package com.services_main.main_bk_service.evaluador.service;


import com.services_main.main_bk_service.evaluador.entity.InterpretacionEntity;
import com.services_main.main_bk_service.evaluador.entity.PreClienteEntity;
import com.services_main.main_bk_service.evaluador.entity.ScoringEntity;
import com.services_main.main_bk_service.evaluador.entity.SolicitudCreditoEntity;
import com.services_main.main_bk_service.evaluador.exceptions.ResourceNotFoundException;
import com.services_main.main_bk_service.evaluador.repository.InterpretacionRepository;
import com.services_main.main_bk_service.evaluador.repository.PreClienteRepository;
import com.services_main.main_bk_service.evaluador.repository.ScoringRepository;
import com.services_main.main_bk_service.evaluador.repository.SolicitudCreditoRepository;
import com.services_main.main_bk_service.evaluador.request.EvaluacionRequest;
import com.services_main.main_bk_service.evaluador.request.PreClienteRequest;
import com.services_main.main_bk_service.evaluador.request.SolicitudCreditoRequest;
import com.services_main.main_bk_service.evaluador.response.EvaluacionResponse;
import com.services_main.main_bk_service.evaluador.response.ModeloActivoResponse;
import com.services_main.main_bk_service.shared.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EvaluacionService implements InEvaluacionService {

    private final WebClient webClient = WebClient.builder().build();

    private final PreClienteRepository preClienteRepo;
    private final SolicitudCreditoRepository solicitudRepo;
    private final ScoringRepository scoringRepo;
    private final InterpretacionRepository interpretacionRepo;


    // Crear pre-cliente
    public Mono<PreClienteEntity> crearPreCliente(PreClienteRequest req) {

        PreClienteEntity p = new PreClienteEntity();
        BeanUtils.copyProperties(req, p);
        p.setCreated(LocalDateTime.now());
        p.setUpdated(LocalDateTime.now());

        return preClienteRepo.save(p)
                .doOnSuccess(c -> System.out.println("Pre-cliente creado: " + c.getId()));
    }

    // Crear solicitud
    public Mono<SolicitudCreditoEntity> crearSolicitud(SolicitudCreditoRequest req) {

        return preClienteRepo.findById(req.getPreClienteId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Pre-cliente no existe")))
                .flatMap(pc -> {

                    SolicitudCreditoEntity s = new SolicitudCreditoEntity();
                    BeanUtils.copyProperties(req, s);
                    s.setFechaSolicitud(LocalDateTime.now());
                    s.setEstadoPredicente("pendiente");

                    return solicitudRepo.save(s)
                            .doOnSuccess(x -> System.out.println("Solicitud creada: {}"+ x.getId()));
                });
    }


    @Override
    public Mono<PreClienteEntity> obtenerPreCliente(Long id) {
        return preClienteRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("PreCliente no encontrado")));
    }


    @Override
    public Flux<PreClienteEntity> listarPreClientes() {
        return preClienteRepo.findAll();
    }

    @Override
    public Mono<SolicitudCreditoEntity> obtenerSolicitud(Long id) {
        return solicitudRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Solicitud no encontrada")));
    }


    @Override
    public Flux<SolicitudCreditoEntity> listarSolicitudesPorPreCliente(Long preClienteId) {
        return solicitudRepo.findByPreClienteId(preClienteId);
    }

    @Override
    public Mono<ScoringEntity> obtenerScoringPorSolicitud(Long solicitudId) {
        return scoringRepo.findBySolicitudId(solicitudId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Scoring no encontrado")));
    }

    @Override
    public Mono<InterpretacionEntity> obtenerInterpretacion(Long scoringId) {
        return interpretacionRepo.findByScoringId(scoringId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Interpretación no encontrada")));
    }

    // (llama a n8n)
    public Mono<EvaluacionResponse> evaluarSolicitud(Long id, EvaluacionRequest req) {

        return solicitudRepo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Solicitud no encontrada")))
                .flatMap(solicitud -> {

                    System.out.println("Evaluando solicitud {} vía N8N: " +  id);

                    return webClient.post()
                            .uri("https://n8nzdr.rdev7.net.pe/webhook/evaluate-risk")
                            .bodyValue(req)
                            .retrieve()
                            .bodyToMono(EvaluacionResponse.class)
                            .flatMap(res -> guardarResultado(id, req, res)
                                    .thenReturn(res));
                });
    }

    // Guardar scoring + interpretación
    private Mono<Void> guardarResultado(Long solicitudId,
                                        EvaluacionRequest req,
                                        EvaluacionResponse res) {

        // 1. Obtener modelo activo desde N8N
        return webClient.get()
                .uri("https://n8nzdr.rdev7.net.pe/webhook/models/active")
                .retrieve()
                .bodyToMono(ModeloActivoResponse.class)
                .flatMap(modelo -> {

                    // 2. Crear scoring con la versión del modelo activo
                    ScoringEntity scoring = new ScoringEntity();
                    scoring.setSolicitudId(solicitudId);
                    scoring.setRiesgo(res.getProbabilidad_incumplimiento());

                    // Inserta versión real
                    scoring.setModeloVersion("v" + modelo.getVersion());

                    scoring.setVariablesJson(JsonUtil.toJson(req));
                    scoring.setFechaEvaluacion(LocalDateTime.now());
                    scoring.setCreated(LocalDateTime.now());

                    // 3. Guardar scoring
                    return scoringRepo.save(scoring)
                            .doOnSuccess(sc ->
                                    System.out.println("Scoring guardado ID: " + sc.getId() +
                                            ", modelo versión: v" + modelo.getVersion())
                            )
                            .flatMap(saved -> {

                                // 4. Crear interpretación
                                InterpretacionEntity i = new InterpretacionEntity();
                                i.setScoringId(saved.getId());
                                i.setResumen(String.join(" | ", res.getMotivos()));
                                i.setFechaGeneracion(LocalDateTime.now());

                                return interpretacionRepo.save(i)
                                        .doOnSuccess(inter ->
                                                System.out.println("Interpretación guardada ID: " + inter.getId())
                                        )
                                        .then();
                            });
                })
                .then()
                .doOnSuccess(v -> System.out.println("Evaluación guardada correctamente"));
    }
}
