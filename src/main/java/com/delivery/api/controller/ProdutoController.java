package com.delivery.api.controller;

import com.delivery.api.dto.produto.ProdutoRequest;
import com.delivery.api.dto.produto.ProdutoResponse;
import com.delivery.api.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Atualiza um produto (somente o restaurante dono do produto)")
    public ResponseEntity<ProdutoResponse> atualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Remove um produto (somente o restaurante dono do produto)")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        produtoService.remover(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
