package com.delivery.api.repository;

import com.delivery.api.model.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    Optional<Entregador> findByUsuarioEmail(String email);
    List<Entregador> findByDisponivelTrue();
}
