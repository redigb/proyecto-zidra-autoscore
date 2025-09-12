package com.services_main.main_bk_service.dispositivoGps.application.service;
import com.services_main.main_bk_service.dispositivoGps.application.port.in.IotiServicePort;
import com.services_main.main_bk_service.dispositivoGps.application.port.out.ArtefactoGpsLinkPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.application.port.out.IotiGpsPersistencePort;
import com.services_main.main_bk_service.dispositivoGps.domain.entity.IotiGps;
import com.services_main.main_bk_service.shared.exception.ConflictException;
import com.services_main.main_bk_service.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IotiGpsService implements IotiServicePort {

    private final IotiGpsPersistencePort iotiGpsPersistencePort;
    private final ArtefactoGpsLinkPersistencePort gpsLinkPersistencePort;

    @Override
    public Mono<IotiGps> registrarDispositivo(IotiGps gps) {
        return iotiGpsPersistencePort.findByDeviceCodeOrImei(gps.getDeviceCode(), gps.getImei())
                .flatMap(existing -> Mono.<IotiGps>error(
                        new RuntimeException("El dispositivo con ese código o IMEI ya está registrado")))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            // Generar username y password
                            String username = "gps" + UUID.randomUUID().toString().substring(0, 5);
                            String password = UUID.randomUUID().toString().substring(0, 8);
                            gps.setCreatedAt(LocalDateTime.now());

                            gps.setMqttUsername(username);
                            gps.setMqttPassword(password);
                            return iotiGpsPersistencePort.saveDispositivo(gps)
                                    .switchIfEmpty(Mono.error(new RuntimeException("Error al registrar el dispositivo")));
                        })
                );
    }

    @Override
    public Mono<IotiGps> obtenerDispositivoPorId(Long id) {
        return iotiGpsPersistencePort.findDispositivoById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Dispositivo no encontrado con id: " + id)));
    }

    @Override
    public Mono<IotiGps> obtenerDispositivoPorImei(String imei) {
        return iotiGpsPersistencePort.findDispositivoByImei(imei)
                .switchIfEmpty(Mono.error(new NotFoundException("Dispositivo no encontrado con IMEI: " + imei)));
    }

    @Override
    public Flux<IotiGps> listarDispositivos() {
        return iotiGpsPersistencePort.findAllDispositivos()
                .switchIfEmpty(Flux.error(new RuntimeException("No hay dispositivos registrados")));
    }

    @Override
    public Mono<IotiGps> actualizarDispositivo(IotiGps gps) {
        return iotiGpsPersistencePort.findDispositivoById(gps.getId())
                .switchIfEmpty(Mono.error(
                        new RuntimeException("No existe dispositivo con id: " + gps.getId())
                )).flatMap(existing -> {
                    existing.setDeviceCode(gps.getDeviceCode());
                    existing.setImei(gps.getImei());
                    existing.setModelo(gps.getModelo());
                    existing.setVersionFirmware(gps.getVersionFirmware());
                    existing.setStatus(gps.getStatus());
                    existing.setSimNumeroTelefono(gps.getSimNumeroTelefono());
                    existing.setSimOperador(gps.getSimOperador());
                    existing.setSimPlan(gps.getSimPlan());
                    return iotiGpsPersistencePort.updateDispositivo(existing);
                });
    }

    @Override
    public Mono<Void> eliminarDispositivo(Long id) {
        return iotiGpsPersistencePort.findDispositivoById(id)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("El dispositivo con id " + id + " no existe")
                ))
                .flatMap(device -> gpsLinkPersistencePort.findActiveByDeviceId(id) // Falta validar si esta ACTIVO el dispositivo
                        .flatMap(link -> Mono.<Void>error(
                                new ConflictException("El dispositivo con id " + id + " está vinculado a un artefacto activo")
                        ))
                        .switchIfEmpty(
                                iotiGpsPersistencePort.deleteDispositivo(id)
                        )
                );
    }
}
