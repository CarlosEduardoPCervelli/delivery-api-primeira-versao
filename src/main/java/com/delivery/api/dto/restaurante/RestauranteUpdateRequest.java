package com.delivery.api.dto.restaurante;

import java.time.LocalTime;

public record RestauranteUpdateRequest(
        String nomeFantasia,
        String categoria,
        LocalTime horaAbertura,
        LocalTime horaFechamento,
        Boolean ativo
) {}
