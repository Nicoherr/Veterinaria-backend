package com.vetsm.backend.controller;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vetsm.backend.dto.AuthResponse;
import com.vetsm.backend.dto.LoginRequest;
import com.vetsm.backend.dto.RegisterRequest;
import com.vetsm.backend.model.Rol;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.UsuarioRepository;
import com.vetsm.backend.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un usuario", description = "Crea una cuenta con rol de usuario estándar y entrega un JWT.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.ROLE_USER)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(createAuthResponse(usuarioRepository.save(usuario)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y entrega un JWT.")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(request.getEmail()))
                .filter(account -> passwordEncoder.matches(request.getPassword(), account.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        return createAuthResponse(usuario);
    }

    private AuthResponse createAuthResponse(Usuario usuario) {
        return new AuthResponse(
                jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name()),
                "Bearer",
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
