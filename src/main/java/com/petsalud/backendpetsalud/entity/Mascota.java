package com.petsalud.backendpetsalud.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "mascotas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 60) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String nombre;

    @Size(max = 40) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String especie;

    @Size(max = 60) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String raza;

    private LocalDate fechaNacimiento;

    @Size(max = 10) @Pattern(regexp = "^[^<>]*$", message = "contiene caracteres no permitidos")
    private String sexo;

    @JsonIgnoreProperties({"telefono", "direccion"})
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<Cita> citas;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<ExpedienteMedico> expedientesMedicos;
}
