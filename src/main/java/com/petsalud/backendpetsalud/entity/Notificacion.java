package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 500) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    @Column(length = 500)
    private String mensaje;

    @Size(max = 30) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String tipo; // Ejemplos: "Promoción", "Recordatorio", "Evento"

    private LocalDateTime fecha;

    private Boolean leida;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
