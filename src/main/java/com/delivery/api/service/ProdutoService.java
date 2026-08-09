package com.delivery.api.service;

import com.delivery.api.dto.produto.ProdutoRequest;
import com.delivery.api.dto.produto.ProdutoResponse;
import com.delivery.api.exception.ResourceNotFoundException;
import com.delivery.api.model.Produto;
import com.delivery.api.model.Restaurante;
import com.delivery.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final RestauranteService restauranteService;

    public Page<ProdutoResponse> listarPorRestaurante(Long restauranteId, Pageable pageable) {
        return produtoRepository.findByRestauranteIdAndDisponivelTrue(restauranteId, pageable)
                .map(ProdutoResponse::from);
    }

    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.from(buscarEntidadePorId(id));
    }

    public Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional
    public ProdutoResponse criar(String emailRestaurante, ProdutoRequest req) {
        Restaurante restaurante = restauranteService.buscarEntidadePorEmail(emailRestaurante);

        Produto produto = Produto.builder()
                .restaurante(restaurante)
                .nome(req.nome())
                .descricao(req.descricao())
                .preco(req.preco())
                .categoria(req.categoria())
                .disponivel(req.disponivel() == null || req.disponivel())
                .build();

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(String emailRestaurante, Long produtoId, ProdutoRequest req) {
        Produto produto = buscarEntidadePorId(produtoId);
        validarDono(produto, emailRestaurante);

        produto.setNome(req.nome());
        produto.setDescricao(req.descricao());
        produto.setPreco(req.preco());
        produto.setCategoria(req.categoria());
        if (req.disponivel() != null) produto.setDisponivel(req.disponivel());

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public void remover(String emailRestaurante, Long produtoId) {
        Produto produto = buscarEntidadePorId(produtoId);
        validarDono(produto, emailRestaurante);
        produtoRepository.delete(produto);
    }

    private void validarDono(Produto produto, String emailRestaurante) {
        if (!produto.getRestaurante().getUsuario().getEmail().equals(emailRestaurante)) {
            throw new ResourceNotFoundException("Produto não encontrado para este restaurante");
        }
    }
}
