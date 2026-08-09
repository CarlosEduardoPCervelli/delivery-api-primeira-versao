package com.delivery.api.repository;

import com.delivery.api.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Page<Produto> findByRestauranteIdAndDisponivelTrue(Long restauranteId, Pageable pageable);
    Page<Produto> findByRestauranteId(Long restauranteId, Pageable pageable);
}
