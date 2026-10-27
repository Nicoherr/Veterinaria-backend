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

import com.vetsm.backend.dto.MascotaRequest;
import com.vetsm.backend.model.Mascota;
import com.vetsm.backend.model.Usuario;
import com.vetsm.backend.repository.MascotaRepository;
import com.vetsm.backend.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascotas", description = "Administración de mascotas del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public MascotaController(MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Listar mis mascotas")
    public List<Mascota> listar(Authentication authentication) {
        return mascotaRepository.findAllByUsuarioEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una mascota")
    public Mascota obtener(@PathVariable Long id, Authentication authentication) {
        return findOwned(id, authentication.getName());
    }

    @PostMapping
    @Operation(summary = "Registrar una mascota")
    public ResponseEntity<Mascota> crear(@Valid @RequestBody MascotaRequest request, Authentication authentication) {
        Usuario usuario = findUser(authentication.getName());
        Mascota mascota = Mascota.builder()
                .nombre(request.getNombre().trim())
                .tipo(request.getTipo().trim())
                .raza(request.getRaza())
                .edad(request.getEdad())
                .microchip(request.getMicrochip())
                .usuario(usuario)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaRepository.save(mascota));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una mascota propia")
    public Mascota actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MascotaRequest request,
            Authentication authentication) {
        Mascota mascota = findOwned(id, authentication.getName());
        mascota.setNombre(request.getNombre().trim());
        mascota.setTipo(request.getTipo().trim());
        mascota.setRaza(request.getRaza());
        mascota.setEdad(request.getEdad());
        mascota.setMicrochip(request.getMicrochip());
        return mascotaRepository.save(mascota);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una mascota propia")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Mascota mascota = findOwned(id, authentication.getName());
        mascotaRepository.delete(mascota);
        return ResponseEntity.noContent().build();
    }

    private Mascota findOwned(Long id, String email) {
        return mascotaRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    }

    private Usuario findUser(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
