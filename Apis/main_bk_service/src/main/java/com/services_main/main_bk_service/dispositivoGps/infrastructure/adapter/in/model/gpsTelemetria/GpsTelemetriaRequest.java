package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.gpsTelemetria;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GpsTelemetriaRequest {

    private Long gpsDeviceId;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaHora;
    private Double speed;
    private Boolean estadoEncendido;
    private JsonNode extraData; // JSON en String

}
