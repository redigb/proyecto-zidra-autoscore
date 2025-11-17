package com.services_main.main_bk_service.evaluador.request;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionRequest {

    private Double monto_total;
    private Double cuota_inicial;
    private Integer plazo_meses;
    private String frecuencia_pago;
    private String tipo_contrato;
    private Integer mes_colocacion;
    private Double bien_precio;
    private Double vehiculo_precio;
    private Integer n_contratos_previos;
    private Integer veces_con_mora_hist;
    private Integer cierre_exitoso;
    private Integer termino_con_deuda;
    private Integer tuvo_mora_pero_pago;
    private Double riesgo_cohorte;

}
