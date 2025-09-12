package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArtefactoGpsLinkResponse {

    private Long id;
    private String pocketbaseArtefactoId;
    private Long gpsDeviceId;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaDesvinculacion;
    private String estado; // ACTIVO, RETIRADO
}
