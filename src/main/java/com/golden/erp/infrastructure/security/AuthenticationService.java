package com.golden.erp.infrastructure.security;

import com.golden.erp.dto.auth.AuthenticationRequest;
import com.golden.erp.dto.auth.AuthenticationResponse;
import com.golden.erp.dto.auth.RegisterRequest;
import com.golden.erp.domain.Usuario;
import com.golden.erp.exception.ConflictException;
import com.golden.erp.infrastructure.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // Verificar se usuário já existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Nome de usuário já existe");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email já cadastrado");
        }

        var usuario = new Usuario(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNomeCompleto(),
                Usuario.Role.USER
        );

        usuarioRepository.save(usuario);

        return AuthenticationResponse.builder()
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nomeCompleto(usuario.getNomeCompleto())
                .role(usuario.getRole().name())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var usuario = usuarioRepository.findByUsernameAndAtivo(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var jwtToken = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nomeCompleto(usuario.getNomeCompleto())
                .role(usuario.getRole().name())
                .build();
    }
}
