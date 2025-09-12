package com.services_main.main_bk_service.dispositivoGps.domain.entity;
import com.services_main.main_bk_service.dispositivoGps.domain.valueObject.EstadoAsignacion;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArtefactoGpsLink {

    private Long id;

    private String pocketbaseArtefactoId;
    private IotiGps iotiGps;

    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaDesvinculacion;

    private EstadoAsignacion estado; // ACTIVO, RETIRADO
}
