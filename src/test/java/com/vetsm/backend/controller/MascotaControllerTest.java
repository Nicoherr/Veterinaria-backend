package com.vetsm.backend.controller;

import java.util.Optional;

import com.vetsm.backend.dto.MascotaRequest;
import com.vetsm.backend.model.Mascota;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.MascotaRepository;
import com.vetsm.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MascotaControllerTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MascotaController mascotaController;

    @Test
    void crearGuardaMascotaAsociadaAlUsuarioAutenticado() {
        Usuario usuario = Usuario.builder().email("tutor@example.com").build();
        MascotaRequest request = new MascotaRequest();
        request.setNombre(" Luna ");
        request.setTipo(" Gato ");
        request.setRaza("Siamés");
        request.setEdad(2);
        request.setMicrochip("CHIP123");
        when(authentication.getName()).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = mascotaController.crear(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Luna", response.getBody().getNombre());
        assertEquals("Gato", response.getBody().getTipo());
        assertSame(usuario, response.getBody().getUsuario());
        verify(mascotaRepository).save(response.getBody());
    }

    @Test
    void obtenerMascotaDeOtroUsuarioRespondeNoEncontrado() {
        when(authentication.getName()).thenReturn("tutor@example.com");
        when(mascotaRepository.findByIdAndUsuarioEmail(42L, "tutor@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mascotaController.obtener(42L, authentication));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
