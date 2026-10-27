package com.vetsm.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetsm.backend.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findAllByUsuarioEmail(String email);

    Optional<Cita> findByIdAndUsuarioEmail(Long id, String email);
}
