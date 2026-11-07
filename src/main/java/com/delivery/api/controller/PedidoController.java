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
    @Operation(summary = "Busca um pedido pelo id (somente cliente, restaurante ou entregador do pedido)")
    public ResponseEntity<PedidoResponse> buscarPorId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(userDetails.getUsername(), id));
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

    @GetMapping("/entregas/disponiveis")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Lista pedidos em preparo que ainda não têm entregador")
    public ResponseEntity<Page<PedidoResponse>> entregasDisponiveis(Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarDisponiveisParaEntrega(pageable));
    }

    @GetMapping("/entregas/me")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Lista as entregas do entregador logado")
    public ResponseEntity<Page<PedidoResponse>> minhasEntregas(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorEntregador(userDetails.getUsername(), pageable));
    }

    @PatchMapping("/{id}/aceitar-entrega")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "O entregador aceita um pedido em preparo (uma entrega por vez)")
    public ResponseEntity<PedidoResponse> aceitarEntrega(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.aceitarEntrega(userDetails.getUsername(), id));
    }

    @PatchMapping("/{id}/confirmar-entrega")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "O entregador responsável confirma que o pedido foi entregue")
    public ResponseEntity<PedidoResponse> confirmarEntrega(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarEntrega(userDetails.getUsername(), id));
    }
}
