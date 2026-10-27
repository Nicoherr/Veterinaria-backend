package com.vetsm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MascotaRequest {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 80)
    private String tipo;

    @Size(max = 120)
    private String raza;

    @PositiveOrZero
    private Integer edad;

    @Size(max = 80)
    private String microchip;
}
