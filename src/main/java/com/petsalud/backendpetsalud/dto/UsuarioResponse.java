package com.petsalud.backendpetsalud.dto;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        String telefono,
        String direccion,
        Rol rol
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getNombre(), u.getApellido(),
                u.getTelefono(), u.getDireccion(), u.getRol());
    }
}
