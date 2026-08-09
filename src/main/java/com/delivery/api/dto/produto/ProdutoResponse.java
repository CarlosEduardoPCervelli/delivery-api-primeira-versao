package com.delivery.api.dto.produto;

import com.delivery.api.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        Long restauranteId,
        String nome,
        String descricao,
        BigDecimal preco,
        String categoria,
        boolean disponivel
) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
                p.getId(),
                p.getRestaurante().getId(),
                p.getNome(),
                p.getDescricao(),
                p.getPreco(),
                p.getCategoria(),
                p.isDisponivel()
        );
    }
}
