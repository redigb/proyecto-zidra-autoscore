package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.artefactoGpsLink;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArtefactoGpsLinkRequest {

    private String pocketbaseArtefactoId;
    private Long gpsDeviceId;

}
