package com.petsalud.backendpetsalud.config;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInicialConfig {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    @Bean
    CommandLineRunner crearAdminInicial(UsuarioRepository usuarioRepository,
                                        PasswordEncoder passwordEncoder,
                                        @Value("${app.security.admin.email}") String email,
                                        @Value("${app.security.admin.password}") String password) {
        return args -> {
            if (usuarioRepository.existsByRol(Rol.ADMIN)) {
                return;
            }
            Usuario admin = Usuario.builder()
                    .email(email.toLowerCase())
                    .password(passwordEncoder.encode(password))
                    .nombre("Administrador")
                    .apellido("PetSalud")
                    .rol(Rol.ADMIN)
                    .build();
            usuarioRepository.save(admin);
            AUDITORIA.info("ADMIN_INICIAL_CREADO usuario={} (cambie la contrasena por defecto)", admin.getEmail());
        };
    }
}
