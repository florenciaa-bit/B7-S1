package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.ActualizarUsuarioRequest;
import com.petsalud.backendpetsalud.dto.RegistroUsuarioRequest;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario registrarUsuario(RegistroUsuarioRequest datos);
    Optional<Usuario> obtenerUsuarioPorId(Long id);
    Optional<Usuario> obtenerUsuarioPorEmail(String email);
    Usuario actualizarUsuario(Long id, ActualizarUsuarioRequest datos);
    Usuario cambiarRol(Long id, Rol rol);
    void eliminarUsuario(Long id);
    List<Usuario> listarUsuarios();
}
