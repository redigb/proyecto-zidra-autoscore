package com.services_main.main_bk_service.evaluador.request;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudCreditoRequest {

    private Long preClienteId;
    private Double montoTotal;
    private Double montoInicial;
    private Integer numeroCuotas;
    private String frecuenciaPago;
    private Double interes;
    private Double montoCuota;
    private Double totalPagar;
    private Long usuarioAsesorId;

}
