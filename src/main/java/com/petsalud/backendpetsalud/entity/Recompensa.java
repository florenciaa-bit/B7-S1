package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @PositiveOrZero
    private Integer puntos;

    @Size(max = 200) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String descripcion;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
