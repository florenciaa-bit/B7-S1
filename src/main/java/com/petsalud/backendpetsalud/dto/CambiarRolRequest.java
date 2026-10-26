package com.petsalud.backendpetsalud.dto;

import com.petsalud.backendpetsalud.entity.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(@NotNull Rol rol) {
}
