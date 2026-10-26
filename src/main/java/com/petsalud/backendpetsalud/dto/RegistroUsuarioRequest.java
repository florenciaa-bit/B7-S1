package com.petsalud.backendpetsalud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotBlank @Email @Size(max = 120)
        String email,

        @NotBlank
        @Size(min = 8, max = 64, message = "debe tener entre 8 y 64 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "debe contener letras y numeros")
        String password,

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
