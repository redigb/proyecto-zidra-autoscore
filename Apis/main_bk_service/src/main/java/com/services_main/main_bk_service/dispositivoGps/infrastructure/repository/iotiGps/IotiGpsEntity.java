package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.iotiGps;

import com.services_main.main_bk_service.dispositivoGps.domain.valueObject.EstadoDispositivo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("ioti_gps")
public class IotiGpsEntity {

    @Id
    private Long id;

    @Column("device_code")
    private String deviceCode;

    @Column("imei")
    private String imei;

    @Column("modelo")
    private String modelo;

    @Column("version_firmware")
    private String versionFirmware;

    @Column("status")
    private EstadoDispositivo status; // ONLINE, OFFLINE

    @Column("sim_numero_telefono")
    private String simNumeroTelefono;  // número telefónico de la línea

    @Column("sim_operador")
    private String simOperador;        // operador (Movistar, Claro, etc.)

    @Column("sim_plan")
    private String simPlan;            // plan contratado (ej. IoT 200MB)

    // 🔑 Campos MQTT
    @Column("mqtt_username")
    private String mqttUsername;
    @Column("mqtt_password")
    private String mqttPassword;

    @Column("created_at")
    private LocalDateTime createdAt;   // fecha de registro en el sistema
}