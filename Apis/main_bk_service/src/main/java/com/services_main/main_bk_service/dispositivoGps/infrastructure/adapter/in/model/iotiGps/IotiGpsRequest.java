package com.services_main.main_bk_service.dispositivoGps.infrastructure.adapter.in.model.iotiGps;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IotiGpsRequest {

    @NotBlank(message = "El código del dispositivo es obligatorio")
    @Size(max = 50, message = "El código del dispositivo no debe superar los 50 caracteres")
    private String deviceCode;

    @Size(max = 50, message = "El IMEI no debe superar los 50 caracteres")
    private String imei; // opcional
    @Size(max = 100, message = "El modelo no debe superar los 100 caracteres")
    private String modelo; // opcional
    @Size(max = 50, message = "La versión de firmware no debe superar los 50 caracteres")
    private String versionFirmware; // opcional

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ONLINE|OFFLINE", message = "El estado debe ser ONLINE u OFFLINE")
    private String status;

    @Size(max = 20, message = "El número telefónico de la SIM no debe superar los 20 caracteres")
    private String simNumeroTelefono; // opcional
    @Size(max = 50, message = "El operador de la SIM no debe superar los 50 caracteres")
    private String simOperador; // opcional
    @Size(max = 100, message = "El plan de la SIM no debe superar los 100 caracteres")
    private String simPlan; // opcional
}