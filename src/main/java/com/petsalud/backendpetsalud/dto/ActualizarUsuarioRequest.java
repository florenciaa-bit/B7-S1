package com.petsalud.backendpetsalud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @NotBlank @Size(max = 60) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
        String nombre,

        @NotBlank @Size(max = 60) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
        String apellido,

        @Size(max = 20) @Pattern(regexp = "^[0-9+ ]*$", message = "solo puede contener numeros, espacios y +")
        String telefono,

        @Size(max = 150) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
        String direccion
) {
}
