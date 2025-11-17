package com.services_main.main_bk_service.evaluador.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionResponse {

    private String nivel_riesgo;
    private List<String> motivos;
    private String recomendacion;
    private Double probabilidad_incumplimiento;

}
