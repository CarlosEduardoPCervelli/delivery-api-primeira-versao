package com.delivery.api.service;

import com.delivery.api.dto.pedido.ItemPedidoRequest;
import com.delivery.api.dto.pedido.PedidoRequest;
import com.delivery.api.dto.pedido.PedidoResponse;
import com.delivery.api.exception.BusinessException;
import com.delivery.api.exception.ResourceNotFoundException;
import com.delivery.api.model.*;
import com.delivery.api.model.enums.StatusPedido;
import com.delivery.api.repository.ClienteRepository;
import com.delivery.api.repository.PedidoRepository;
import com.delivery.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RestauranteService restauranteService;

    /** Transições de status permitidas. */
    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = new EnumMap<>(StatusPedido.class);
    static {
        TRANSICOES_VALIDAS.put(StatusPedido.RECEBIDO, EnumSet.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO));
        TRANSICOES_VALIDAS.put(StatusPedido.EM_PREPARO, EnumSet.of(StatusPedido.SAIU_PARA_ENTREGA, StatusPedido.CANCELADO));
        TRANSICOES_VALIDAS.put(StatusPedido.SAIU_PARA_ENTREGA, EnumSet.of(StatusPedido.ENTREGUE));
        TRANSICOES_VALIDAS.put(StatusPedido.ENTREGUE, EnumSet.noneOf(StatusPedido.class));
        TRANSICOES_VALIDAS.put(StatusPedido.CANCELADO, EnumSet.noneOf(StatusPedido.class));
    }

    @Transactional
    public PedidoResponse criar(String emailCliente, PedidoRequest req) {
        Cliente cliente = clienteRepository.findByUsuarioEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado para o usuário logado"));

        Restaurante restaurante = restauranteService.buscarEntidadePorId(req.restauranteId());

        if (!restaurante.estaAberto()) {
            throw new BusinessException("O restaurante está fechado no momento");
        }

        Endereco enderecoEntrega = new Endereco(req.rua(), req.numero(), req.complemento(), req.bairro(), req.cidade(), req.cep());

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .status(StatusPedido.RECEBIDO)
                .enderecoEntrega(enderecoEntrega)
                .dataCriacao(LocalDateTime.now())
                .build();

        for (ItemPedidoRequest itemReq : req.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemReq.produtoId()));

            if (!produto.getRestaurante().getId().equals(restaurante.getId())) {
                throw new BusinessException("O produto '" + produto.getNome() + "' não pertence ao restaurante informado");
            }
            if (!produto.isDisponivel()) {
                throw new BusinessException("O produto '" + produto.getNome() + "' não está disponível no momento");
            }

            // Preço nunca vem do cliente: sempre lido do produto no servidor (snapshot no momento da compra)
            ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPreco())
                    .build();

            pedido.adicionarItem(item);
        }

        pedido.recalcularValorTotal();
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    public Page<PedidoResponse> listarPorCliente(String emailCliente, Pageable pageable) {
        Cliente cliente = clienteRepository.findByUsuarioEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado para o usuário logado"));
        return pedidoRepository.findByClienteId(cliente.getId(), pageable).map(PedidoResponse::from);
    }

    public Page<PedidoResponse> listarPorRestaurante(String emailRestaurante, StatusPedido status, Pageable pageable) {
        Restaurante restaurante = restauranteService.buscarEntidadePorEmail(emailRestaurante);
        Page<Pedido> pagina = status != null
                ? pedidoRepository.findByRestauranteIdAndStatus(restaurante.getId(), status, pageable)
                : pedidoRepository.findByRestauranteId(restaurante.getId(), pageable);
        return pagina.map(PedidoResponse::from);
    }

    public PedidoResponse buscarPorId(Long id) {
        return PedidoResponse.from(buscarEntidadePorId(id));
    }

    public Pedido buscarEntidadePorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    @Transactional
    public PedidoResponse atualizarStatus(String emailRestaurante, Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        if (!pedido.getRestaurante().getUsuario().getEmail().equals(emailRestaurante)) {
            throw new ResourceNotFoundException("Pedido não encontrado para este restaurante");
        }

        StatusPedido statusAtual = pedido.getStatus();
        Set<StatusPedido> permitidos = TRANSICOES_VALIDAS.get(statusAtual);

        if (!permitidos.contains(novoStatus)) {
            throw new BusinessException(
                    "Transição de status inválida: " + statusAtual + " -> " + novoStatus);
        }

        pedido.setStatus(novoStatus);
        pedido.setDataAtualizacao(LocalDateTime.now());
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }
}
