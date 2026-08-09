package com.delivery.api.repository;

import com.delivery.api.model.Pedido;
import com.delivery.api.model.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);
    Page<Pedido> findByRestauranteId(Long restauranteId, Pageable pageable);
    Page<Pedido> findByRestauranteIdAndStatus(Long restauranteId, StatusPedido status, Pageable pageable);
    Page<Pedido> findByEntregadorId(Long entregadorId, Pageable pageable);
}
