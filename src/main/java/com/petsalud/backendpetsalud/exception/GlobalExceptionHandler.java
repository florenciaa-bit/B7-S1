package com.petsalud.backendpetsalud.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.security.Principal;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        Map<String, Object> cuerpo = cuerpo(HttpStatus.BAD_REQUEST, "Datos invalidos");
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(cuerpo(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler({DateTimeParseException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> formatoInvalido(Exception ex) {
        return ResponseEntity.badRequest().body(cuerpo(HttpStatus.BAD_REQUEST, "Formato de solicitud invalido"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        Principal usuario = request.getUserPrincipal();
        AUDITORIA.warn("ACCESO_DENEGADO usuario={} metodo={} ruta={} ip={}",
                usuario != null ? usuario.getName() : "anonimo",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(cuerpo(HttpStatus.FORBIDDEN, "No tiene permisos para esta accion"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> errorInterno(Exception ex) {
        if (ex instanceof ErrorResponse errorHttp) {
            HttpStatus estado = HttpStatus.valueOf(errorHttp.getStatusCode().value());
            return ResponseEntity.status(estado).body(cuerpo(estado, estado.getReasonPhrase()));
        }
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor"));
    }

    private Map<String, Object> cuerpo(HttpStatus estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("error", mensaje);
        return cuerpo;
    }
}
