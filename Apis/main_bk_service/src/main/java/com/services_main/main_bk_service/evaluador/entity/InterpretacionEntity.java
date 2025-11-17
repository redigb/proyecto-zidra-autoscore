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
@Table("interpretacion")
public class InterpretacionEntity {

    @Id
    private Long id;

    @Column("scoring_id")
    private Long scoringId;

    private String resumen;

    @Column("fecha_generacion")
    private LocalDateTime fechaGeneracion;

}
