package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.ActualizarUsuarioRequest;
import com.petsalud.backendpetsalud.dto.RegistroUsuarioRequest;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrarUsuario(RegistroUsuarioRequest datos) {
        String email = datos.email().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }
        Usuario usuario = Usuario.builder()
                .email(email)
                // La contrasena se guarda como hash BCrypt, nunca en texto plano
                .password(passwordEncoder.encode(datos.password()))
                .nombre(datos.nombre().trim())
                .apellido(datos.apellido().trim())
                .telefono(datos.telefono())
                .direccion(datos.direccion())
                // El registro publico siempre crea clientes
                .rol(Rol.CLIENTE)
                .build();
        Usuario guardado = usuarioRepository.save(usuario);
        AUDITORIA.info("USUARIO_REGISTRADO id={} rol={}", guardado.getId(), guardado.getRol());
        return guardado;
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    public Usuario actualizarUsuario(Long id, ActualizarUsuarioRequest datos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datos.nombre().trim());
            usuario.setApellido(datos.apellido().trim());
            usuario.setTelefono(datos.telefono());
            usuario.setDireccion(datos.direccion());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public Usuario cambiarRol(Long id, Rol rol) {
        return usuarioRepository.findById(id).map(usuario -> {
            Rol anterior = usuario.getRol();
            usuario.setRol(rol);
            Usuario guardado = usuarioRepository.save(usuario);
            AUDITORIA.info("ROL_CAMBIADO usuarioId={} de={} a={}", id, anterior, rol);
            return guardado;
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
        AUDITORIA.info("USUARIO_ELIMINADO id={}", id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
