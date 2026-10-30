package com.vetsm.backend.controller;

import java.util.Optional;

import com.vetsm.backend.dto.LoginRequest;
import com.vetsm.backend.model.Rol;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.UsuarioRepository;
import com.vetsm.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginNormalizaElCorreoYDevuelveLosCamposEsperadosPorElFrontend() {
        Usuario usuario = Usuario.builder()
                .email("tutor@vetsm.cl")
                .nombre("Tutor")
                .password("hash")
                .rol(Rol.ROLE_USER)
                .build();
        LoginRequest request = new LoginRequest();
        request.setEmail(" Tutor@VetSM.cl ");
        request.setPassword("password-seguro");
        when(usuarioRepository.findByEmail("tutor@vetsm.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password-seguro", "hash")).thenReturn(true);
        when(jwtUtil.generateToken("tutor@vetsm.cl", "ROLE_USER")).thenReturn("jwt-test");

        var response = authController.login(request);

        assertEquals("jwt-test", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("tutor@vetsm.cl", response.email());
        assertEquals("Tutor", response.nombre());
        assertEquals(Rol.ROLE_USER, response.rol());
        verify(usuarioRepository).findByEmail("tutor@vetsm.cl");
    }

    @Test
    void loginRechazaCredencialesIncorrectas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("tutor@vetsm.cl");
        request.setPassword("incorrecta");
        Usuario usuario = Usuario.builder().password("hash").build();
        when(usuarioRepository.findByEmail("tutor@vetsm.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
