package com.delivery.api.service;

import com.delivery.api.dto.pedido.ItemPedidoRequest;
import com.delivery.api.dto.pedido.PedidoRequest;
import com.delivery.api.dto.pedido.PedidoResponse;
import com.delivery.api.exception.BusinessException;
import com.delivery.api.model.*;
import com.delivery.api.model.enums.StatusPedido;
import com.delivery.api.repository.ClienteRepository;
import com.delivery.api.repository.PedidoRepository;
import com.delivery.api.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private RestauranteService restauranteService;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private Restaurante restauranteAberto;
    private Produto produto;

    @BeforeEach
    void setUp() {
        Usuario usuarioCliente = Usuario.builder().id(1L).nome("João").email("joao@teste.com").build();
        cliente = Cliente.builder().id(1L).usuario(usuarioCliente).telefone("11999990000").build();

        restauranteAberto = Restaurante.builder()
                .id(10L)
                .nomeFantasia("Pizzaria do Zé")
                .ativo(true)
                .horaAbertura(LocalTime.of(0, 0))
                .horaFechamento(LocalTime.of(23, 59))
                .build();

        produto = Produto.builder()
                .id(100L)
                .restaurante(restauranteAberto)
                .nome("Pizza Margherita")
                .preco(new BigDecimal("45.90"))
                .disponivel(true)
                .build();
    }

    @Test
    void deveCriarPedidoComValorTotalCalculadoNoServidor() {
        PedidoRequest request = new PedidoRequest(
                10L,
                List.of(new ItemPedidoRequest(100L, 2)),
                "Rua A", "123", null, "Centro", "São Paulo", "01000-000"
        );

        when(clienteRepository.findByUsuarioEmail("joao@teste.com")).thenReturn(Optional.of(cliente));
        when(restauranteService.buscarEntidadePorId(10L)).thenReturn(restauranteAberto);
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.criar("joao@teste.com", request);

        assertThat(response.valorTotal()).isEqualByComparingTo("91.80");
        assertThat(response.status()).isEqualTo(StatusPedido.RECEBIDO);
    }

    @Test
    void naoDevePermitirPedidoEmRestauranteFechado() {
        Restaurante fechado = Restaurante.builder()
                .id(20L)
                .nomeFantasia("Fechado 24h")
                .ativo(false)
                .build();

        PedidoRequest request = new PedidoRequest(
                20L,
                List.of(new ItemPedidoRequest(100L, 1)),
                "Rua A", "123", null, "Centro", "São Paulo", "01000-000"
        );

        when(clienteRepository.findByUsuarioEmail("joao@teste.com")).thenReturn(Optional.of(cliente));
        when(restauranteService.buscarEntidadePorId(20L)).thenReturn(fechado);

        assertThatThrownBy(() -> pedidoService.criar("joao@teste.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fechado");
    }

    @Test
    void naoDevePermitirTransicaoDeStatusInvalida() {
        Usuario usuarioRestaurante = Usuario.builder().email("dono@pizzaria.com").build();
        restauranteAberto.setUsuario(usuarioRestaurante);

        Pedido pedidoEntregue = Pedido.builder()
                .id(1L)
                .cliente(cliente)
                .restaurante(restauranteAberto)
                .status(StatusPedido.ENTREGUE)
                .valorTotal(BigDecimal.TEN)
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntregue));

        assertThatThrownBy(() -> pedidoService.atualizarStatus("dono@pizzaria.com", 1L, StatusPedido.EM_PREPARO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }
}
