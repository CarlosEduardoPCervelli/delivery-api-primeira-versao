package com.delivery.api.dto.restaurante;

import com.delivery.api.model.Restaurante;

import java.time.LocalTime;

public record RestauranteResponse(
        Long id,
        String nomeFantasia,
        String categoria,
        String cidade,
        LocalTime horaAbertura,
        LocalTime horaFechamento,
        boolean aberto
) {
    public static RestauranteResponse from(Restaurante r) {
        return new RestauranteResponse(
                r.getId(),
                r.getNomeFantasia(),
                r.getCategoria(),
                r.getEndereco() != null ? r.getEndereco().getCidade() : null,
                r.getHoraAbertura(),
                r.getHoraFechamento(),
                r.estaAberto()
        );
    }
}
