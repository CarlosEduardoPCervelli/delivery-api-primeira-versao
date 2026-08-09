package com.delivery.api.repository;

import com.delivery.api.model.Restaurante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    Optional<Restaurante> findByUsuarioEmail(String email);

    Page<Restaurante> findByCategoriaIgnoreCaseAndAtivoTrue(String categoria, Pageable pageable);

    Page<Restaurante> findByNomeFantasiaContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

    Page<Restaurante> findByAtivoTrue(Pageable pageable);
}
