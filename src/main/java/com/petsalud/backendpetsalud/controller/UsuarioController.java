package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.ActualizarUsuarioRequest;
import com.petsalud.backendpetsalud.dto.CambiarRolRequest;
import com.petsalud.backendpetsalud.dto.RegistroUsuarioRequest;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import com.petsalud.backendpetsalud.security.AccesoGuard;
import com.petsalud.backendpetsalud.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AccesoGuard acceso;

    public UsuarioController(UsuarioService usuarioService, AccesoGuard acceso) {
        this.usuarioService = usuarioService;
        this.acceso = acceso;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.desde(usuarioService.registrarUsuario(datos)));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> usuarioActual() {
        return usuarioService.obtenerUsuarioPorId(acceso.usuarioActualId())
                .map(u -> ResponseEntity.ok(UsuarioResponse.desde(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or @acceso.esElMismoUsuario(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(u -> ResponseEntity.ok(UsuarioResponse.desde(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or @acceso.esElMismoUsuario(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable Long id,
                                                             @Valid @RequestBody ActualizarUsuarioRequest datos) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.actualizarUsuario(id, datos)));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest datos) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.cambiarRol(id, datos.rol())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios().stream().map(UsuarioResponse::desde).toList());
    }
}
