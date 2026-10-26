package com.petsalud.backendpetsalud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String VETERINARIO = "VETERINARIO";

    @Value("${app.security.cors.allowed-origins}")
    private List<String> origenesPermitidos;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UsuarioDetailsService usuarioDetailsService,
                                                   RestAuthHandlers authHandlers) throws Exception {

        
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // ---------- Publico ----------
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/stock-bajo").hasAnyRole(VETERINARIO, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ---------- Solo administrador (gerencia) ----------
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole(ADMIN)
                        .requestMatchers("/api/usuarios/*/rol").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/productos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/ventas").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/recompensas/**", "/api/notificaciones/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/recompensas/**", "/api/notificaciones/**",
                                "/api/expedientes/**").hasRole(ADMIN)

                        // ---------- Personal clinico (veterinario o administrador) ----------
                        .requestMatchers(HttpMethod.POST, "/api/expedientes").hasAnyRole(VETERINARIO, ADMIN)
                        .requestMatchers("/api/citas/veterinario/**", "/api/citas/entre-fechas",
                                "/api/citas/*/estado").hasAnyRole(VETERINARIO, ADMIN)

                        // ---------- Todo lo demas requiere iniciar sesion ----------
                        .anyRequest().authenticated()
                )
                .userDetailsService(usuarioDetailsService)
                // API sin sesiones: cada peticion trae sus credenciales
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(authHandlers))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authHandlers)
                        .accessDeniedHandler(authHandlers))
                // Encabezados de seguridad contra XSS, clickjacking y cache de datos sensibles
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .frameOptions(frame -> frame.deny()));

        return http.build();
    }

    /** BCrypt con factor de costo 12: las contrasenas nunca se guardan en texto plano. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Publica eventos de login exitoso / fallido para la auditoria y el bloqueo de cuentas. */
    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    /** Solo los frontends conocidos de PetSalud pueden llamar a la API desde un navegador. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
