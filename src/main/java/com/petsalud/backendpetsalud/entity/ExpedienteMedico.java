package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "expedientes_medicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpedienteMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @NotBlank @Size(max = 1000) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    @Column(length = 1000)
    private String descripcion;

    @Size(max = 1000) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    @Column(length = 1000)
    private String diagnostico;

    @Size(max = 1000) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    @Column(length = 1000)
    private String tratamiento;

    
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    @JsonIgnoreProperties({"telefono", "direccion"})
    private Usuario veterinario;
}
