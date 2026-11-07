package com.delivery.api.repository;

import com.delivery.api.model.Entregador;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {
    Optional<Entregador> findByUsuarioEmail(String email);
    List<Entregador> findByDisponivelTrue();

    /** Busca com lock de escrita: impede o mesmo entregador de aceitar duas entregas em paralelo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Entregador e where e.usuario.email = :email")
    Optional<Entregador> findByEmailForUpdate(@Param("email") String email);
}
