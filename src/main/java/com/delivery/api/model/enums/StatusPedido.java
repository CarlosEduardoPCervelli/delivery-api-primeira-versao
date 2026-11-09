package com.delivery.api.model.enums;

/**
 * Máquina de estados do pedido.
 * Transições válidas:
 * RECEBIDO -> EM_PREPARO -> SAIU_PARA_ENTREGA -> ENTREGUE
 * RECEBIDO ou EM_PREPARO -> CANCELADO
 *
 * Quem avança cada etapa:
 * - RESTAURANTE: RECEBIDO -> EM_PREPARO, EM_PREPARO -> SAIU_PARA_ENTREGA (exige entregador atribuído) e cancelamentos
 * - ENTREGADOR: aceita pedidos EM_PREPARO e confirma SAIU_PARA_ENTREGA -> ENTREGUE
 */
public enum StatusPedido {
    RECEBIDO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO
}
