package com.delivery.api.dto.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(
        @NotNull Long produtoId,
        @NotNull @Min(1) Integer quantidade
) {}
