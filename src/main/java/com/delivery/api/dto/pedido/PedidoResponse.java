package com.delivery.api.dto.pedido;

import com.delivery.api.model.Pedido;
import com.delivery.api.model.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        Long restauranteId,
        String nomeRestaurante,
        Long entregadorId,
        List<ItemPedidoResponse> itens,
        StatusPedido status,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(
                p.getId(),
                p.getCliente().getId(),
                p.getRestaurante().getId(),
                p.getRestaurante().getNomeFantasia(),
                p.getEntregador() != null ? p.getEntregador().getId() : null,
                p.getItens().stream().map(ItemPedidoResponse::from).toList(),
                p.getStatus(),
                p.getValorTotal(),
                p.getDataCriacao(),
                p.getDataAtualizacao()
        );
    }
}
