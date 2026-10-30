package com.vetsm.backend.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.vetsm.backend.dto.CitaRequest;
import com.vetsm.backend.model.Cita;
import com.vetsm.backend.model.Mascota;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.CitaRepository;
import com.vetsm.backend.repository.MascotaRepository;
import com.vetsm.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CitaController citaController;

    @Test
    void crearCitaAsociaMascotaPropiaYUsuarioAutenticado() {
        String email = "tutor@example.com";
        Usuario usuario = Usuario.builder().email(email).build();
        Mascota mascota = Mascota.builder().id(7L).nombre("Luna").usuario(usuario).build();
        CitaRequest request = new CitaRequest();
        request.setMascotaId(7L);
        request.setFecha(LocalDate.of(2030, 1, 15));
        request.setHora(LocalTime.of(10, 30));
        request.setMotivo("Control");
        when(authentication.getName()).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(mascotaRepository.findByIdAndUsuarioEmail(7L, email)).thenReturn(Optional.of(mascota));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = citaController.crear(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(request.getFecha(), response.getBody().getFecha());
        assertEquals(request.getHora(), response.getBody().getHora());
        assertEquals(request.getMotivo(), response.getBody().getMotivo());
        assertSame(mascota, response.getBody().getMascota());
        assertSame(usuario, response.getBody().getUsuario());
        verify(citaRepository).save(response.getBody());
    }
}
