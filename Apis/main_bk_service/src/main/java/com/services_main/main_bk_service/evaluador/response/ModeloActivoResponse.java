package com.services_main.main_bk_service.evaluador.response;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModeloActivoResponse {

    private Integer version;
    private String mode;
    private Double auc;
    private Double ks;
    private Double f1;
    private Double precision;
    private Double recall;
    private String created_at;
}
