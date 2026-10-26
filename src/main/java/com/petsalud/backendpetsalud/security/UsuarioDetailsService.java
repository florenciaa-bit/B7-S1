package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository, LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                // Mensaje generico: no revelar si el email existe o no
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        return new UsuarioDetails(usuario, loginAttemptService.estaBloqueado(usuario.getEmail()));
    }
}
