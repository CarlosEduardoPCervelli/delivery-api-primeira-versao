package com.delivery.api.dto.pedido;

import com.delivery.api.model.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizaStatusRequest(
        @NotNull StatusPedido status
) {}
