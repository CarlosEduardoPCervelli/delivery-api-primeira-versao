package com.delivery.api.model.enums;

/**
 * Máquina de estados do pedido.
 * Transições válidas:
 * RECEBIDO -> EM_PREPARO -> SAIU_PARA_ENTREGA -> ENTREGUE
 * RECEBIDO ou EM_PREPARO -> CANCELADO
 */
public enum StatusPedido {
    RECEBIDO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO
}
