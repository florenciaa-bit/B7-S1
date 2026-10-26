package com.petsalud.backendpetsalud.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    private final int maxIntentos;
    private final Duration duracionBloqueo;
    private final Map<String, Intentos> intentosPorUsuario = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${app.security.max-intentos}") int maxIntentos,
                               @Value("${app.security.minutos-bloqueo}") long minutosBloqueo) {
        this.maxIntentos = maxIntentos;
        this.duracionBloqueo = Duration.ofMinutes(minutosBloqueo);
    }

    @EventListener
    public void alIniciarSesion(AuthenticationSuccessEvent evento) {
        String usuario = evento.getAuthentication().getName().toLowerCase();
        intentosPorUsuario.remove(usuario);
        AUDITORIA.info("LOGIN_OK usuario={}", usuario);
    }

    @EventListener
    public void alFallarInicioSesion(AbstractAuthenticationFailureEvent evento) {
        String usuario = String.valueOf(evento.getAuthentication().getName()).toLowerCase();
        String motivo = evento.getException().getClass().getSimpleName();
        Intentos intentos = intentosPorUsuario.computeIfAbsent(usuario, u -> new Intentos());
        int total = intentos.registrarFallo();
        AUDITORIA.warn("LOGIN_FALLIDO usuario={} motivo={} intentos={}", usuario, motivo, total);
        if (total >= maxIntentos && intentos.bloqueadoHasta == null) {
            intentos.bloqueadoHasta = Instant.now().plus(duracionBloqueo);
            AUDITORIA.warn("CUENTA_BLOQUEADA usuario={} minutos={}", usuario, duracionBloqueo.toMinutes());
        }
    }

    public boolean estaBloqueado(String usuario) {
        Intentos intentos = intentosPorUsuario.get(usuario.toLowerCase());
        if (intentos == null || intentos.bloqueadoHasta == null) {
            return false;
        }
        if (Instant.now().isAfter(intentos.bloqueadoHasta)) {
            intentosPorUsuario.remove(usuario.toLowerCase());
            return false;
        }
        return true;
    }

    private static class Intentos {
        private int fallos;
        private Instant bloqueadoHasta;

        synchronized int registrarFallo() {
            return ++fallos;
        }
    }
}
