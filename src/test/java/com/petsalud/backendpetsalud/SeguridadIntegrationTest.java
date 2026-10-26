package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas automaticas que demuestran que la configuracion de Spring Security
 * funciona. Usan una base de datos H2 en memoria (no necesitan MySQL).
 * Ejecutar con: mvnw test
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIntegrationTest {

    private static final String ADMIN = "admin@petsalud.cl";
    private static final String ADMIN_PASS = "Admin.PetSalud2026";
    private static final String CLAVE = "Clave1234";

    @Autowired private MockMvc mvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario ana;
    private Usuario beto;
    private Mascota mascotaDeBeto;

    @BeforeEach
    void preparar() {
        mascotaRepository.deleteAll();
        usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != Rol.ADMIN)
                .forEach(usuarioRepository::delete);
        ana = crearUsuario("ana@correo.cl", Rol.CLIENTE);
        beto = crearUsuario("beto@correo.cl", Rol.CLIENTE);
        mascotaDeBeto = mascotaRepository.save(Mascota.builder()
                .nombre("Firulais").especie("Perro").propietario(beto).build());
    }

    private Usuario crearUsuario(String email, Rol rol) {
        return usuarioRepository.save(Usuario.builder()
                .email(email).password(passwordEncoder.encode(CLAVE))
                .nombre("Test").apellido("Usuario").rol(rol).build());
    }

    // ---------------------------------------------------------------- Registro

    @Test
    @DisplayName("Registro: crea CLIENTE aunque se envie rol ADMIN, guarda hash BCrypt y no devuelve la contrasena")
    void registroSeguro() throws Exception {
        String cuerpo = """
                {"email":"nuevo@correo.cl","password":"MiClave2026","nombre":"Nuevo",
                 "apellido":"Cliente","rol":"ADMIN"}
                """;
        mvc.perform(post("/api/usuarios/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());

        Usuario guardado = usuarioRepository.findByEmail("nuevo@correo.cl").orElseThrow();
        assertThat(guardado.getPassword()).startsWith("$2a$12$").isNotEqualTo("MiClave2026");
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    @DisplayName("Registro: rechaza contrasena debil y texto con etiquetas HTML (XSS)")
    void registroValidaDatos() throws Exception {
        String cuerpo = """
                {"email":"x@correo.cl","password":"123","nombre":"<script>alert(1)</script>","apellido":"A"}
                """;
        mvc.perform(post("/api/usuarios/registro").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.password").exists())
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    // ---------------------------------------------------------------- Autenticacion

    @Test
    @DisplayName("Sin credenciales la API responde 401")
    void sinCredenciales() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con usuario y contrasena correctos responde 200")
    void loginCorrecto() throws Exception {
        mvc.perform(get("/api/usuarios/me").with(httpBasic("ana@correo.cl", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@correo.cl"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Contrasena incorrecta responde 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(get("/api/usuarios/me").with(httpBasic("ana@correo.cl", "otraClave1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Tras 5 intentos fallidos la cuenta queda bloqueada, incluso con la contrasena correcta")
    void bloqueoPorFuerzaBruta() throws Exception {
        crearUsuario("victima@correo.cl", Rol.CLIENTE);
        for (int i = 0; i < 5; i++) {
            mvc.perform(get("/api/usuarios/me").with(httpBasic("victima@correo.cl", "Incorrecta" + i)))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/usuarios/me").with(httpBasic("victima@correo.cl", CLAVE)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- Autorizacion por rol

    @Test
    @DisplayName("Un CLIENTE no puede listar usuarios; un ADMIN si")
    void listarUsuariosSoloAdmin() throws Exception {
        mvc.perform(get("/api/usuarios").with(httpBasic("ana@correo.cl", CLAVE)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").with(httpBasic(ADMIN, ADMIN_PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    @DisplayName("Un CLIENTE no puede crear productos ni registros clinicos")
    void clienteSinPermisosDePersonal() throws Exception {
        mvc.perform(post("/api/productos").with(csrf()).with(httpBasic("ana@correo.cl", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Collar\",\"precio\":1000,\"stock\":5}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/expedientes").with(csrf()).with(httpBasic("ana@correo.cl", CLAVE))
                        .param("mascotaId", mascotaDeBeto.getId().toString())
                        .param("veterinarioId", ana.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"falso\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Solo un ADMIN puede asignar el rol VETERINARIO")
    void cambioDeRol() throws Exception {
        String cuerpo = "{\"rol\":\"VETERINARIO\"}";
        mvc.perform(put("/api/usuarios/" + ana.getId() + "/rol").with(csrf()).with(httpBasic("ana@correo.cl", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/" + ana.getId() + "/rol").with(csrf()).with(httpBasic(ADMIN, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VETERINARIO"));
    }

    // ---------------------------------------------------------------- Autorizacion por dueno (IDOR)

    @Test
    @DisplayName("Un cliente no puede ver la mascota de otro cliente; el dueno y el veterinario si")
    void mascotaSoloDueno() throws Exception {
        String url = "/api/mascotas/" + mascotaDeBeto.getId();
        mvc.perform(get(url).with(httpBasic("ana@correo.cl", CLAVE))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(httpBasic("beto@correo.cl", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.propietario.password").doesNotExist());

        crearUsuario("vet@correo.cl", Rol.VETERINARIO);
        mvc.perform(get(url).with(httpBasic("vet@correo.cl", CLAVE))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un cliente no puede ver el historial clinico de una mascota ajena")
    void historialClinicoSoloDueno() throws Exception {
        mvc.perform(get("/api/expedientes/mascota/" + mascotaDeBeto.getId()).with(httpBasic("ana@correo.cl", CLAVE)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- CSRF, SQL injection, encabezados

    @Test
    @DisplayName("Un POST sin token CSRF es rechazado con 403")
    void csrfObligatorio() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"c@correo.cl\",\"password\":\"Clave1234\",\"nombre\":\"C\",\"apellido\":\"C\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un intento de inyeccion SQL se trata como texto y no devuelve datos")
    void inyeccionSql() throws Exception {
        mvc.perform(get("/api/productos/categoria").param("categoria", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Las respuestas incluyen encabezados de seguridad (XSS, clickjacking, cache)")
    void encabezadosDeSeguridad() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }
}
