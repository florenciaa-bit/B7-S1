package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String nombre;

    @Size(max = 500) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    @Column(length = 500)
    private String descripcion;

    @NotNull @PositiveOrZero
    private BigDecimal precio;

    @NotNull @PositiveOrZero
    private Integer stock;

    @Size(max = 60) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String categoria;
}

