package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("acceso")
public class AccesoGuard {

    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ExpedienteMedicoRepository expedienteRepository;
    private final VentaRepository ventaRepository;
    private final RecompensaRepository recompensaRepository;
    private final NotificacionRepository notificacionRepository;

    public AccesoGuard(MascotaRepository mascotaRepository, CitaRepository citaRepository,
                       ExpedienteMedicoRepository expedienteRepository, VentaRepository ventaRepository,
                       RecompensaRepository recompensaRepository, NotificacionRepository notificacionRepository) {
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.expedienteRepository = expedienteRepository;
        this.ventaRepository = ventaRepository;
        this.recompensaRepository = recompensaRepository;
        this.notificacionRepository = notificacionRepository;
    }

    public Long usuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails detalles) {
            return detalles.getId();
        }
        return null;
    }

    public boolean esElMismoUsuario(Long usuarioId) {
        Long actual = usuarioActualId();
        return actual != null && actual.equals(usuarioId);
    }

    public boolean esDuenoMascota(Long mascotaId) {
        return mascotaRepository.findById(mascotaId)
                .map(m -> m.getPropietario() != null && esElMismoUsuario(m.getPropietario().getId()))
                .orElse(false);
    }

    public boolean esDuenoCita(Long citaId) {
        return citaRepository.findById(citaId)
                .map(c -> c.getMascota() != null && esDuenoMascota(c.getMascota().getId()))
                .orElse(false);
    }

    public boolean esDuenoExpediente(Long expedienteId) {
        return expedienteRepository.findById(expedienteId)
                .map(e -> e.getMascota() != null && esDuenoMascota(e.getMascota().getId()))
                .orElse(false);
    }

    public boolean esDuenoVenta(Long ventaId) {
        return ventaRepository.findById(ventaId)
                .map(v -> v.getUsuario() != null && esElMismoUsuario(v.getUsuario().getId()))
                .orElse(false);
    }

    public boolean esDuenoRecompensa(Long recompensaId) {
        return recompensaRepository.findById(recompensaId)
                .map(r -> r.getUsuario() != null && esElMismoUsuario(r.getUsuario().getId()))
                .orElse(false);
    }

    public boolean esDuenoNotificacion(Long notificacionId) {
        return notificacionRepository.findById(notificacionId)
                .map(n -> n.getUsuario() != null && esElMismoUsuario(n.getUsuario().getId()))
                .orElse(false);
    }
}
