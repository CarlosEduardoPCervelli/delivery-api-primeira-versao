package com.delivery.api.dto.auth;

public record AuthResponse(
        String token,
        String tipo,
        String nome,
        String email,
        String perfil
) {
    public AuthResponse(String token, String nome, String email, String perfil) {
        this(token, "Bearer", nome, email, perfil);
    }
}
