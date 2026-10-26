package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @acceso.esElMismoUsuario(#propietarioId)")
    @PostMapping("/propietario/{propietarioId}")
    public ResponseEntity<Mascota> registrarMascota(@PathVariable Long propietarioId, @Valid @RequestBody Mascota mascota) {
        Mascota nuevaMascota = mascotaService.registrarMascota(mascota, propietarioId);
        return ResponseEntity.ok(nuevaMascota);
    }

    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @acceso.esDuenoMascota(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerMascotaPorId(@PathVariable Long id) {
        return mascotaService.obtenerMascotaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @acceso.esDuenoMascota(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable Long id, @Valid @RequestBody Mascota mascota) {
        Mascota mascotaActualizada = mascotaService.actualizarMascota(id, mascota);
        return ResponseEntity.ok(mascotaActualizada);
    }

    @PreAuthorize("hasRole('ADMIN') or @acceso.esDuenoMascota(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @acceso.esElMismoUsuario(#propietarioId)")
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> listarMascotasPorPropietario(@PathVariable Long propietarioId) {
        List<Mascota> mascotas = mascotaService.listarMascotasPorPropietario(propietarioId);
        return ResponseEntity.ok(mascotas);
    }
}
