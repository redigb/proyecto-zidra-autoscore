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
@Table("solicitud_credito")
public class SolicitudCreditoEntity {

    @Id
    private Long id;

    @Column("pre_cliente_id")
    private Long preClienteId;

    @Column("monto_total")
    private Double montoTotal;

    @Column("monto_inicial")
    private Double montoInicial;

    @Column("numero_cuotas")
    private Integer numeroCuotas;

    @Column("frecuencia_pago")
    private String frecuenciaPago;

    private Double interes;

    @Column("monto_cuota")
    private Double montoCuota;

    @Column("total_pagar")
    private Double totalPagar;

    @Column("estado_predicente")
    private String estadoPredicente; // pendiente, evaluada, aprobada, rechazada

    @Column("fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column("usuario_asesor_id")
    private Long usuarioAsesorId;

    private LocalDateTime updated;
}
