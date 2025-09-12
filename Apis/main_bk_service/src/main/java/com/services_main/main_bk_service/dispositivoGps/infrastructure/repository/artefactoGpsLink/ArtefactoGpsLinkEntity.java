package com.services_main.main_bk_service.dispositivoGps.infrastructure.repository.artefactoGpsLink;

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
@Table("artefacto_gps_link")
public class ArtefactoGpsLinkEntity {

    @Id
    private Long id;

    @Column("pocketbase_artefacto_id")
    private String pocketbaseArtefactoId;
    @Column("gps_device_id")
    private Long gpsDeviceId; // FK hacia la tabla gps_device
    @Column("fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column("fecha_desvinculacion")
    private LocalDateTime fechaDesvinculacion;

    @Column("estado")
    private String estado; // ACTIVO / RETIRADO (guardado como String en BD)

}
