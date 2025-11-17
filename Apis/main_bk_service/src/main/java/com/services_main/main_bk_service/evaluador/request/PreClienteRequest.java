package com.services_main.main_bk_service.evaluador.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PreClienteRequest {

    private String numeroDni;
    private String nombre;
    private String apellido;
    private String direccion;
    private String referenciaVivienda;
    private String distrito;
    private String provincia;
    private String departamento;
    private String numeroCelular;
    private String numeroCelularSecundario;
    private String correoElectronico;

}
