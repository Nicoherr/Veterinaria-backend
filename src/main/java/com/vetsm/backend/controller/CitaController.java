package com.vetsm.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vetsm.backend.dto.CitaRequest;
import com.vetsm.backend.model.Cita;
import com.vetsm.backend.model.Mascota;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.CitaRepository;
import com.vetsm.backend.repository.MascotaRepository;
import com.vetsm.backend.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Administración de citas veterinarias del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CitaController {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaController(
            CitaRepository citaRepository,
            MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Listar mis citas")
    public List<Cita> listar(Authentication authentication) {
        return citaRepository.findAllByUsuarioEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cita")
    public Cita obtener(@PathVariable Long id, Authentication authentication) {
        return findOwned(id, authentication.getName());
    }

    @PostMapping
    @Operation(summary = "Agendar una cita para una mascota propia")
    public ResponseEntity<Cita> crear(@Valid @RequestBody CitaRequest request, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = findUser(email);
        Mascota mascota = findOwnedPet(request.getMascotaId(), email);
        Cita cita = Cita.builder()
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .mascota(mascota)
                .usuario(usuario)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(citaRepository.save(cita));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una cita propia")
    public Cita actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Cita cita = findOwned(id, email);
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setMotivo(request.getMotivo());
        cita.setMascota(findOwnedPet(request.getMascotaId(), email));
        return citaRepository.save(cita);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una cita propia")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Cita cita = findOwned(id, authentication.getName());
        citaRepository.delete(cita);
        return ResponseEntity.noContent().build();
    }

    private Cita findOwned(Long id, String email) {
        return citaRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    private Mascota findOwnedPet(Long id, String email) {
        return mascotaRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    }

    private Usuario findUser(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
