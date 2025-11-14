package com.services_main.main_bk_service.dispositivoGps.domain.entity;
import com.fasterxml.jackson.databind.JsonNode;
import com.services_main.main_bk_service.dispositivoGps.domain.valueObject.EstadoDispositivo;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IotiGps {

    private Long id;

    private String deviceCode;
    private String imei;

    private String modelo;
    private String versionFirmware;
    private EstadoDispositivo status; // ONLINE, OFFLINE

    private String simNumeroTelefono;  // número telefónico de la línea
    private String simOperador;     // operador (Movistar, Claro, etc.)
    private String simPlan;         // plan contratado (ej. IoT 200MB)

    // Campos MQTT
    private String mqttUsername;
    private String mqttPassword;

    private LocalDateTime createdAt;
}
