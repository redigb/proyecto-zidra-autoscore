package com.services_main.main_bk_service.evaluador.entity;
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
@NoArgsConstructor
@AllArgsConstructor
@Table("scoring")
public class ScoringEntity {
    @Id
    private Long id;

    @Column("solicitud_id")
    private Long solicitudId;

    private Double riesgo; // probabilidad 0 a 1

    @Column("modelo_version")
    private String modeloVersion;

    @Column("variables_json")
    private String variablesJson;

    @Column("fecha_evaluacion")
    private LocalDateTime fechaEvaluacion;

    private LocalDateTime created;
    private LocalDateTime updated;
}
