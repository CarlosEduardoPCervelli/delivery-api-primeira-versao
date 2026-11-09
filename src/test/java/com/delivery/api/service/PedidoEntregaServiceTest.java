package com.delivery.api.service;

import com.delivery.api.dto.pedido.PedidoResponse;
import com.delivery.api.exception.BusinessException;
import com.delivery.api.exception.ResourceNotFoundException;
import com.delivery.api.model.Cliente;
import com.delivery.api.model.Entregador;
import com.delivery.api.model.Pedido;
import com.delivery.api.model.Restaurante;
import com.delivery.api.model.Usuario;
import com.delivery.api.model.enums.StatusPedido;
import com.delivery.api.repository.ClienteRepository;
import com.delivery.api.repository.EntregadorRepository;
import com.delivery.api.repository.PedidoRepository;
import com.delivery.api.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoEntregaServiceTest {

    private static final String EMAIL_CLIENTE = "cliente@teste.com";
    private static final String EMAIL_DONO = "dono@pizzaria.com";
    private static final String EMAIL_ENTREGADOR = "moto@teste.com";

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private RestauranteService restauranteService;
    @Mock
    private EntregadorRepository entregadorRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private Restaurante restaurante;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(1L).usuario(usuario(EMAIL_CLIENTE)).telefone("11999990000").build();
        restaurante = Restaurante.builder().id(10L).nomeFantasia("Pizzaria do Zé").usuario(usuario(EMAIL_DONO)).build();
    }

    // ------------------------------------------------------------------ //
    // aceitarEntrega
    // ------------------------------------------------------------------ //

    @Test
    void entregadorDeveAceitarPedidoEmPreparoEFicarIndisponivel() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, true);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, null);

        when(entregadorRepository.findByEmailForUpdate(EMAIL_ENTREGADOR)).thenReturn(Optional.of(entregador));
        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.aceitarEntrega(EMAIL_ENTREGADOR, 1L);

        assertThat(response.entregadorId()).isEqualTo(5L);
        assertThat(entregador.isDisponivel()).isFalse();
        verify(entregadorRepository).save(entregador);
    }

    @Test
    void entregadorOcupadoNaoPodeAceitarOutraEntrega() {
        Entregador ocupado = entregador(5L, EMAIL_ENTREGADOR, false);
        when(entregadorRepository.findByEmailForUpdate(EMAIL_ENTREGADOR)).thenReturn(Optional.of(ocupado));

        assertThatThrownBy(() -> pedidoService.aceitarEntrega(EMAIL_ENTREGADOR, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entrega em andamento");

        verify(pedidoRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void naoDeveAceitarPedidoQueJaTemEntregador() {
        Entregador outro = entregador(6L, "outro@teste.com", false);
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, true);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, outro);

        when(entregadorRepository.findByEmailForUpdate(EMAIL_ENTREGADOR)).thenReturn(Optional.of(entregador));
        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.aceitarEntrega(EMAIL_ENTREGADOR, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi aceito");

        assertThat(entregador.isDisponivel()).isTrue();
    }

    @Test
    void naoDeveAceitarPedidoQueAindaNaoEstaEmPreparo() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, true);
        Pedido pedido = pedido(StatusPedido.RECEBIDO, null);

        when(entregadorRepository.findByEmailForUpdate(EMAIL_ENTREGADOR)).thenReturn(Optional.of(entregador));
        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.aceitarEntrega(EMAIL_ENTREGADOR, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_PREPARO");
    }

    // ------------------------------------------------------------------ //
    // confirmarEntrega
    // ------------------------------------------------------------------ //

    @Test
    void entregadorResponsavelDeveConfirmarEntregaEFicarDisponivel() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.SAIU_PARA_ENTREGA, entregador);

        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.confirmarEntrega(EMAIL_ENTREGADOR, 1L);

        assertThat(response.status()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(entregador.isDisponivel()).isTrue();
    }

    @Test
    void outroEntregadorNaoPodeConfirmarEntregaDoPedido() {
        Entregador responsavel = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.SAIU_PARA_ENTREGA, responsavel);

        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarEntrega("outro@teste.com", 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA);
    }

    @Test
    void naoDeveConfirmarEntregaDePedidoQueNaoSaiuParaEntrega() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, entregador);

        when(pedidoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarEntrega(EMAIL_ENTREGADOR, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("saiu para entrega");
    }

    // ------------------------------------------------------------------ //
    // atualizarStatus (restaurante) com o fluxo do entregador
    // ------------------------------------------------------------------ //

    @Test
    void restauranteNaoPodeDespacharPedidoSemEntregador() {
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, null);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(EMAIL_DONO, 1L, StatusPedido.SAIU_PARA_ENTREGA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sem um entregador");
    }

    @Test
    void restauranteDeveDespacharPedidoComEntregadorAtribuido() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, entregador);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.atualizarStatus(EMAIL_DONO, 1L, StatusPedido.SAIU_PARA_ENTREGA);

        assertThat(response.status()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA);
    }

    @Test
    void restauranteNaoPodeMarcarPedidoComoEntregue() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.SAIU_PARA_ENTREGA, entregador);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(EMAIL_DONO, 1L, StatusPedido.ENTREGUE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entregador responsável");
    }

    @Test
    void cancelarPedidoComEntregadorLiberaOEntregador() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, entregador);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.atualizarStatus(EMAIL_DONO, 1L, StatusPedido.CANCELADO);

        assertThat(response.status()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(entregador.isDisponivel()).isTrue();
    }

    // ------------------------------------------------------------------ //
    // buscarPorId: só participantes do pedido
    // ------------------------------------------------------------------ //

    @Test
    void clienteRestauranteEEntregadorDoPedidoPodemConsultar() {
        Entregador entregador = entregador(5L, EMAIL_ENTREGADOR, false);
        Pedido pedido = pedido(StatusPedido.EM_PREPARO, entregador);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThat(pedidoService.buscarPorId(EMAIL_CLIENTE, 1L).id()).isEqualTo(1L);
        assertThat(pedidoService.buscarPorId(EMAIL_DONO, 1L).id()).isEqualTo(1L);
        assertThat(pedidoService.buscarPorId(EMAIL_ENTREGADOR, 1L).id()).isEqualTo(1L);
    }

    @Test
    void usuarioDeForaDoPedidoNaoPodeConsultar() {
        Pedido pedido = pedido(StatusPedido.RECEBIDO, null);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.buscarPorId("curioso@teste.com", 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ //
    // helpers
    // ------------------------------------------------------------------ //

    private Usuario usuario(String email) {
        return Usuario.builder().email(email).build();
    }

    private Entregador entregador(Long id, String email, boolean disponivel) {
        return Entregador.builder().id(id).usuario(usuario(email)).veiculo("Moto").disponivel(disponivel).build();
    }

    private Pedido pedido(StatusPedido status, Entregador entregador) {
        return Pedido.builder()
                .id(1L)
                .cliente(cliente)
                .restaurante(restaurante)
                .entregador(entregador)
                .status(status)
                .valorTotal(BigDecimal.TEN)
                .build();
    }
}
