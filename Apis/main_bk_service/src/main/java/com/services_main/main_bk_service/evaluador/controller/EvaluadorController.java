package com.services_main.main_bk_service.evaluador.controller;

import com.services_main.main_bk_service.evaluador.entity.InterpretacionEntity;
import com.services_main.main_bk_service.evaluador.entity.PreClienteEntity;
import com.services_main.main_bk_service.evaluador.entity.ScoringEntity;
import com.services_main.main_bk_service.evaluador.entity.SolicitudCreditoEntity;
import com.services_main.main_bk_service.evaluador.request.EvaluacionRequest;
import com.services_main.main_bk_service.evaluador.request.PreClienteRequest;
import com.services_main.main_bk_service.evaluador.request.SolicitudCreditoRequest;
import com.services_main.main_bk_service.evaluador.response.EvaluacionResponse;
import com.services_main.main_bk_service.evaluador.service.InEvaluacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/evaluador")
public class EvaluadorController {

    private final InEvaluacionService evaluacionService;

    @PostMapping("/pre-cliente")
    public Mono<PreClienteEntity> crearPreCliente(@RequestBody PreClienteRequest req) {
        return evaluacionService.crearPreCliente(req);
    }

    @PostMapping("/solicitud")
    public Mono<SolicitudCreditoEntity> crearSolicitud(@RequestBody SolicitudCreditoRequest req) {
        return evaluacionService.crearSolicitud(req);
    }

    @PostMapping("/solicitud/{id}/evaluar")
    public Mono<EvaluacionResponse> evaluarSolicitud(
            @PathVariable Long id,
            @RequestBody EvaluacionRequest req) {
        return evaluacionService.evaluarSolicitud(id, req);
    }


    @GetMapping("/pre-cliente/{id}")
    public Mono<PreClienteEntity> obtenerPreCliente(@PathVariable Long id) {
        return evaluacionService.obtenerPreCliente(id);
    }

    // 2. Listar todos los preclientes
    @GetMapping("/pre-cliente")
    public Flux<PreClienteEntity> listarPreClientes() {
        return evaluacionService.listarPreClientes();
    }

    // 3. Obtener solicitud por id
    @GetMapping("/solicitud/{id}")
    public Mono<SolicitudCreditoEntity> obtenerSolicitud(@PathVariable Long id) {
        return evaluacionService.obtenerSolicitud(id);
    }

    // 4. Listar solicitudes de un precliente
    @GetMapping("/pre-cliente/{id}/solicitudes")
    public Flux<SolicitudCreditoEntity> listarSolicitudesPorPreCliente(@PathVariable Long id) {
        return evaluacionService.listarSolicitudesPorPreCliente(id);
    }

    // 5. Obtener scoring por solicitud
    @GetMapping("/solicitud/{id}/scoring")
    public Mono<ScoringEntity> obtenerScoringPorSolicitud(@PathVariable Long id) {
        return evaluacionService.obtenerScoringPorSolicitud(id);
    }

    // 6. Obtener interpretación por scoring
    @GetMapping("/scoring/{id}/interpretacion")
    public Mono<InterpretacionEntity> obtenerInterpretacion(@PathVariable Long id) {
        return evaluacionService.obtenerInterpretacion(id);
    }
}
