package com.vetsm.backend.dto;

import com.vetsm.backend.model.Rol;

public record AuthResponse(String token, String tokenType, String email, String nombre, Rol rol) {
}
