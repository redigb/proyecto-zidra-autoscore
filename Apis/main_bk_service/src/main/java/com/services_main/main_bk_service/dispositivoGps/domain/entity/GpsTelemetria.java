package com.services_main.main_bk_service.dispositivoGps.domain.entity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GpsTelemetria {

    private Long id;

    private IotiGps gpsDevice;
    private LocalDateTime fechaHora;

    private Double latitud;
    private Double longitud;

    private Double speed;
    private Boolean estadoEncendido;

    private JsonNode extraData; // JSON string
}
