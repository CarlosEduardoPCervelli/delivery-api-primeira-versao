package com.delivery.api.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
        @NotNull Long restauranteId,
        @NotEmpty List<@Valid ItemPedidoRequest> itens,

        @NotBlank String rua,
        @NotBlank String numero,
        String complemento,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank String cep
) {}
