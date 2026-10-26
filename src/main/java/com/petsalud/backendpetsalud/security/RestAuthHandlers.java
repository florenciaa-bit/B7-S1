package com.petsalud.backendpetsalud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Principal;

@Component
public class RestAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        AUDITORIA.warn("NO_AUTENTICADO metodo={} ruta={} ip={}",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        Principal usuario = request.getUserPrincipal();
        AUDITORIA.warn("ACCESO_DENEGADO usuario={} metodo={} ruta={} ip={}",
                usuario != null ? usuario.getName() : "anonimo",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        escribir(response, HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para esta accion");
    }

    private void escribir(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"estado\":" + estado + ",\"error\":\"" + mensaje + "\"}");
    }
}
