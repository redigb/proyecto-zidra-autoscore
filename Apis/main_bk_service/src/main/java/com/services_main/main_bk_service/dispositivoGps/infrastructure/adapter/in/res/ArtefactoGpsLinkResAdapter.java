package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.res;

import com.services_main.main_bk_service.dispositivoGps.application.port.in.ArtefactoLinkServicePort;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink.ArtefactoGpsLinkRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink.ArtefactoGpsLinkResponse;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.artefactoGpsLink.ArtefactoGpsLinkRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${api.prefix}/artefacto-gps-link")
public class ArtefactoGpsLinkResAdapter {

    private final ArtefactoLinkServicePort artefactoLinkServicePort;
    private final ArtefactoGpsLinkRestMapper mapper;

    @PostMapping
    public Mono<ArtefactoGpsLinkResponse> vincular(@RequestBody ArtefactoGpsLinkRequest request) {
        return artefactoLinkServicePort.vincularGpsAArtefacto(
                        request.getPocketbaseArtefactoId(),
                        request.getGpsDeviceId()
                )
                .map(mapper::toResponse);
    }

    @GetMapping("/artefacto/{artefactoId}")
    public Flux<ArtefactoGpsLinkResponse> obtenerPorArtefacto(@PathVariable String artefactoId) {
        return artefactoLinkServicePort.obtenerVinculoPorArtefacto(artefactoId)
                .map(mapper::toResponse);
    }

    @GetMapping("/gps/{gpsDeviceId}/activo")
    public Mono<ArtefactoGpsLinkResponse> obtenerActivoPorGps(@PathVariable Long gpsDeviceId) {
        return artefactoLinkServicePort.obtenerVinculoActivoPorDispositivo(gpsDeviceId)
                .map(mapper::toResponse);
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<ArtefactoGpsLinkResponse>>> listarVinculos() {
        return artefactoLinkServicePort.listarVinculos()
                .map(resp -> {
                    if (resp.getStatusCode().is2xxSuccessful() && resp.hasBody()) {
                        // Mapear Domain → Response DTO
                        Flux<ArtefactoGpsLinkResponse> body = resp.getBody()
                                .map(link -> mapper.toResponse(link));
                        return ResponseEntity.ok(body);
                    }
                    return ResponseEntity.noContent().build();
                });
    }

    @PutMapping("/{linkId}/desvincular")
    public Mono<ArtefactoGpsLinkResponse> desvincular(@PathVariable Long linkId) {
        return artefactoLinkServicePort.desvincularGps(linkId)
                .map(mapper::toResponse);
    }

}
