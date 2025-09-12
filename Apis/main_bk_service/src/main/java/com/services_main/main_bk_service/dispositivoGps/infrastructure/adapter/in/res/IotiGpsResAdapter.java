package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.res;

import com.services_main.main_bk_service.dispositivoGps.application.port.in.IotiServicePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps.IotiGpsRequest;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps.IotiGpsResponse;
import com.services_main.main_bk_service.dispositivoGps.infrastructure.mappers.iotiGps.IotiGpsRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${api.prefix}/ioti-gps")
public class IotiGpsResAdapter {

    private final IotiServicePort iotiServicePort;
    private final IotiGpsRestMapper mapper;

    @PostMapping
    public Mono<IotiGpsResponse> registrar(@RequestBody IotiGpsRequest request) {
        return iotiServicePort.registrarDispositivo(mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<IotiGpsResponse> obtenerPorId(@PathVariable Long id) {
        return iotiServicePort.obtenerDispositivoPorId(id)
                .map(mapper::toResponse);
    }

    @GetMapping("/imei/{imei}")
    public Mono<IotiGpsResponse> obtenerPorImei(@PathVariable String imei) {
        return iotiServicePort.obtenerDispositivoPorImei(imei)
                .map(mapper::toResponse);
    }

    @GetMapping
    public Flux<IotiGpsResponse> listar() {
        return iotiServicePort.listarDispositivos()
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<IotiGpsResponse> actualizar(@PathVariable Long id, @RequestBody IotiGpsRequest request) {
        IotiGps gps = mapper.toDomain(request);
        gps.setId(id);
        return iotiServicePort.actualizarDispositivo(gps)
                .map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> eliminar(@PathVariable Long id) {
        return iotiServicePort.eliminarDispositivo(id);
    }

}
