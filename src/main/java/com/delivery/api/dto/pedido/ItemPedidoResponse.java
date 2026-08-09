package com.delivery.api.dto.pedido;

import com.delivery.api.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
        );
    }
}
