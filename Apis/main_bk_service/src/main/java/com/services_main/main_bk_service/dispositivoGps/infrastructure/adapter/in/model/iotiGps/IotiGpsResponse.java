package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IotiGpsResponse {

    private Long id;
    private String deviceCode;
    private String imei;
    private String modelo;
    private String versionFirmware;
    private String status;

    private String simNumeroTelefono;
    private String simOperador;
    private String simPlan;

    // 🔑 Campos MQTT // --> SE DEBE SOLICITAR APARTE ESTOS DATOS Y DEBE PERMITIRSE ACTUALIZARSE
    private String mqttUsername;
    private String mqttPassword;

    private LocalDateTime createdAt;
}
