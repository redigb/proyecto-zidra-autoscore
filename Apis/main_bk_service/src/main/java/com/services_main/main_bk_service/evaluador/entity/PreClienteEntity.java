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
@Table("pre_cliente")
public class PreClienteEntity {

    @Id
    private Long id;

    @Column("numero_dni")
    private String numeroDni;

    private String nombre;
    private String apellido;
    private String direccion;

    @Column("referencia_vivienda")
    private String referenciaVivienda;

    private String distrito;
    private String provincia;
    private String departamento;

    @Column("numero_celular")
    private String numeroCelular;

    @Column("numero_celular_secundario")
    private String numeroCelularSecundario;

    @Column("correo_electronico")
    private String correoElectronico;

    private LocalDateTime created;
    private LocalDateTime updated;

}
