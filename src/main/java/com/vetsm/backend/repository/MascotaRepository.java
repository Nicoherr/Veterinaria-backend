package com.vetsm.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetsm.backend.model.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findAllByUsuarioEmail(String email);

    Optional<Mascota> findByIdAndUsuarioEmail(Long id, String email);
}
