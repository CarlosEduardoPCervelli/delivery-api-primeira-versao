package com.delivery.api.controller;

import com.delivery.api.dto.produto.ProdutoRequest;
import com.delivery.api.dto.produto.ProdutoResponse;
import com.delivery.api.dto.restaurante.RestauranteResponse;
import com.delivery.api.dto.restaurante.RestauranteUpdateRequest;
import com.delivery.api.service.ProdutoService;
import com.delivery.api.service.RestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurantes")
@RequiredArgsConstructor
@Tag(name = "Restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;
    private final ProdutoService produtoService;

    @GetMapping
    @Operation(summary = "Lista restaurantes ativos, com filtro opcional por categoria ou nome")
    public ResponseEntity<Page<RestauranteResponse>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String nome,
            Pageable pageable) {
        return ResponseEntity.ok(restauranteService.listar(categoria, nome, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um restaurante pelo id")
    public ResponseEntity<RestauranteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(restauranteService.buscarPorId(id));
    }

    @GetMapping("/{id}/produtos")
    @Operation(summary = "Lista os produtos disponíveis de um restaurante")
    public ResponseEntity<Page<ProdutoResponse>> listarProdutos(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarPorRestaurante(id, pageable));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Atualiza o perfil do restaurante logado")
    public ResponseEntity<RestauranteResponse> atualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody RestauranteUpdateRequest request) {
        return ResponseEntity.ok(restauranteService.atualizarPerfil(userDetails.getUsername(), request));
    }

    @PostMapping("/me/produtos")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Cadastra um novo produto no restaurante logado")
    public ResponseEntity<ProdutoResponse> criarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.criar(userDetails.getUsername(), request));
    }
}
