package com.delivery.api.controller;

import com.delivery.api.dto.pedido.AtualizaStatusRequest;
import com.delivery.api.dto.pedido.PedidoRequest;
import com.delivery.api.dto.pedido.PedidoResponse;
import com.delivery.api.model.enums.StatusPedido;
import com.delivery.api.service.PedidoService;
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
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Cria um novo pedido para o cliente logado")
    public ResponseEntity<PedidoResponse> criar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.criar(userDetails.getUsername(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Lista os pedidos do cliente logado")
    public ResponseEntity<Page<PedidoResponse>> meusPedidos(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(userDetails.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pedido pelo id")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping("/restaurante/me")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Lista os pedidos recebidos pelo restaurante logado, com filtro opcional de status")
    public ResponseEntity<Page<PedidoResponse>> pedidosDoRestaurante(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) StatusPedido status,
            Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorRestaurante(userDetails.getUsername(), status, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANTE')")
    @Operation(summary = "Atualiza o status de um pedido, respeitando a máquina de estados")
    public ResponseEntity<PedidoResponse> atualizarStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AtualizaStatusRequest request) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(userDetails.getUsername(), id, request.status()));
    }
}
