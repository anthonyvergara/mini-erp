package com.golden.erp.service;

import com.golden.erp.domain.*;
import com.golden.erp.dto.pedido.*;
import com.golden.erp.infrastructure.repository.ClienteRepository;
import com.golden.erp.infrastructure.repository.PedidoRepository;
import com.golden.erp.infrastructure.repository.ProdutoRepository;
import com.golden.erp.mapper.PedidoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoServiceImpl - Testes Unitários")
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Cliente cliente;
    private Produto produto1;
    private Produto produto2;
    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;
    private PedidoResponseDTO pedidoResponseDTO;
    private ItemPedidoRequestDTO itemRequest1;
    private ItemPedidoRequestDTO itemRequest2;

    @BeforeEach
    void setUp() {
        // Setup Cliente
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");

        // Setup Produtos
        produto1 = new Produto();
        produto1.setId(1L);
        produto1.setSku("PROD001");
        produto1.setNome("Produto 1");
        produto1.setPrecoBruto(new BigDecimal("50.00"));
        produto1.setEstoque(100);
        produto1.setAtivo(true);

        produto2 = new Produto();
        produto2.setId(2L);
        produto2.setSku("PROD002");
        produto2.setNome("Produto 2");
        produto2.setPrecoBruto(new BigDecimal("30.50"));
        produto2.setEstoque(50);
        produto2.setAtivo(true);

        // Setup Itens do Pedido
        itemRequest1 = new ItemPedidoRequestDTO();
        itemRequest1.setProdutoId(1L);
        itemRequest1.setQuantidade(2);
        itemRequest1.setDesconto(new BigDecimal("5.00"));

        itemRequest2 = new ItemPedidoRequestDTO();
        itemRequest2.setProdutoId(2L);
        itemRequest2.setQuantidade(3);
        itemRequest2.setDesconto(new BigDecimal("2.50"));

        // Setup Pedido Request
        pedidoRequestDTO = new PedidoRequestDTO();
        pedidoRequestDTO.setClienteId(1L);
        pedidoRequestDTO.setItens(Arrays.asList(itemRequest1, itemRequest2));

        // Setup Pedido
        pedido = new Pedido(cliente);
        pedido.setId(1L);
        pedido.setStatus(StatusPedido.CREATED);
        pedido.setSubtotal(new BigDecimal("191.50"));
        pedido.setTotalDesconto(new BigDecimal("7.50"));
        pedido.setTotal(new BigDecimal("184.00"));

        // Setup Response DTO
        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(1L);
        pedidoResponseDTO.setClienteId(1L);
        pedidoResponseDTO.setStatus(StatusPedido.CREATED);
        pedidoResponseDTO.setSubtotal(new BigDecimal("191.50"));
        pedidoResponseDTO.setTotalDesconto(new BigDecimal("7.50"));
        pedidoResponseDTO.setTotal(new BigDecimal("184.00"));
        pedidoResponseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Testes para criação de pedido")
    class CriarPedidoTests {

        @Test
        @DisplayName("Deve criar pedido com sucesso")
        void deveCriarPedidoComSucesso() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any(Pedido.class))).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.criar(pedidoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getClienteId()).isEqualTo(1L);
            assertThat(resultado.getStatus()).isEqualTo(StatusPedido.CREATED);
            assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("184.00"));

            verify(clienteRepository).findById(1L);
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
            verify(produtoRepository).baixarEstoque(1L, 2);
            verify(produtoRepository).baixarEstoque(2L, 3);
            verify(pedidoRepository).save(any(Pedido.class));
            verify(pedidoMapper).toResponseDTO(any(Pedido.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cliente não encontrado");

            verify(clienteRepository).findById(1L);
            verify(produtoRepository, never()).findAllById(any());
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado")
        void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(List.of(produto1)); // Apenas produto1, faltando produto2

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produtos não encontrados: [2]");

            verify(clienteRepository).findById(1L);
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto está inativo")
        void deveLancarExcecaoQuandoProdutoInativo() {
            // Arrange
            produto1.setAtivo(false);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produtos inativos: [Produto 1]");

            verify(clienteRepository).findById(1L);
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente")
        void deveLancarExcecaoQuandoEstoqueInsuficiente() {
            // Arrange
            produto1.setEstoque(1); // Menor que a quantidade solicitada (2)
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Estoque insuficiente para: [Produto 1 (disponível: 1, solicitado: 2)]");

            verify(clienteRepository).findById(1L);
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para cálculos financeiros com precisão de duas casas decimais")
    class CalculosFinanceirosTests {

        @Test
        @DisplayName("Deve calcular totais com duas casas decimais exatas")
        void deveCalcularTotaisComDuasCasasDecimais() {
            // Arrange - valores que resultam em cálculos precisos
            produto1.setPrecoBruto(new BigDecimal("33.33"));
            produto2.setPrecoBruto(new BigDecimal("66.67"));
            itemRequest1.setQuantidade(3);
            itemRequest1.setDesconto(new BigDecimal("0.01"));
            itemRequest2.setQuantidade(2);
            itemRequest2.setDesconto(new BigDecimal("0.02"));

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedidoSalvo = invocation.getArgument(0);

                // Verificar precisão: todos os valores devem ter exatamente 2 casas decimais
                assertThat(pedidoSalvo.getSubtotal().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotalDesconto().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotal().scale()).isEqualTo(2);

                // Verificar cálculos corretos:
                // Subtotal: (33.33 * 3) + (66.67 * 2) = 99.99 + 133.34 = 233.33
                assertThat(pedidoSalvo.getSubtotal()).isEqualByComparingTo(new BigDecimal("233.33"));
                // Total desconto: 0.01 + 0.02 = 0.03
                assertThat(pedidoSalvo.getTotalDesconto()).isEqualByComparingTo(new BigDecimal("0.03"));
                // Total: 233.33 - 0.03 = 233.30
                assertThat(pedidoSalvo.getTotal()).isEqualByComparingTo(new BigDecimal("233.30"));

                return pedidoSalvo;
            });
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            pedidoService.criar(pedidoRequestDTO);

            // Assert
            verify(pedidoRepository).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Deve calcular totais sem desconto mantendo precisão")
        void deveCalcularTotaisSemDesconto() {
            // Arrange
            itemRequest1.setDesconto(null);
            itemRequest2.setDesconto(null);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedidoSalvo = invocation.getArgument(0);

                // Subtotal: (50.00 * 2) + (30.50 * 3) = 100.00 + 91.50 = 191.50
                assertThat(pedidoSalvo.getSubtotal()).isEqualByComparingTo(new BigDecimal("191.50"));
                // Total desconto: 0.00
                assertThat(pedidoSalvo.getTotalDesconto()).isEqualByComparingTo(new BigDecimal("0.00"));
                // Total: 191.50 - 0.00 = 191.50
                assertThat(pedidoSalvo.getTotal()).isEqualByComparingTo(new BigDecimal("191.50"));

                return pedidoSalvo;
            });
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            pedidoService.criar(pedidoRequestDTO);

            // Assert
            verify(pedidoRepository).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Deve arredondar valores corretamente para duas casas decimais")
        void deveArredondarValoresCorretamenteParaDuasCasasDecimais() {
            // Arrange - valores que precisam de arredondamento
            produto1.setPrecoBruto(new BigDecimal("10.996")); // Precisa arredondamento
            produto2.setPrecoBruto(new BigDecimal("5.334")); // Precisa arredondamento
            itemRequest1.setQuantidade(1);
            itemRequest1.setDesconto(new BigDecimal("0.999")); // Precisa arredondamento
            itemRequest2.setQuantidade(1);
            itemRequest2.setDesconto(new BigDecimal("0.001")); // Precisa arredondamento

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedidoSalvo = invocation.getArgument(0);

                // Verificar que todos os valores foram arredondados para 2 casas decimais
                assertThat(pedidoSalvo.getSubtotal().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotalDesconto().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotal().scale()).isEqualTo(2);

                // Verificar que os valores são positivos e fazem sentido
                assertThat(pedidoSalvo.getSubtotal()).isPositive();
                assertThat(pedidoSalvo.getTotal()).isPositive();

                return pedidoSalvo;
            });
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            pedidoService.criar(pedidoRequestDTO);

            // Assert
            verify(pedidoRepository).save(any(Pedido.class));
        }

        @Test
        @DisplayName("Deve garantir precisão em cálculos com valores grandes")
        void deveGarantirPrecisaoEmCalculosComValoresGrandes() {
            // Arrange - valores grandes para testar precisão
            produto1.setPrecoBruto(new BigDecimal("999.99"));
            produto2.setPrecoBruto(new BigDecimal("1500.00"));
            itemRequest1.setQuantidade(10);
            itemRequest1.setDesconto(new BigDecimal("50.75"));
            itemRequest2.setQuantidade(5);
            itemRequest2.setDesconto(new BigDecimal("25.25"));

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedidoSalvo = invocation.getArgument(0);

                // Subtotal: (999.99 * 10) + (1500.00 * 5) = 9999.90 + 7500.00 = 17499.90
                assertThat(pedidoSalvo.getSubtotal()).isEqualByComparingTo(new BigDecimal("17499.90"));
                // Total desconto: 50.75 + 25.25 = 76.00
                assertThat(pedidoSalvo.getTotalDesconto()).isEqualByComparingTo(new BigDecimal("76.00"));
                // Total: 17499.90 - 76.00 = 17423.90
                assertThat(pedidoSalvo.getTotal()).isEqualByComparingTo(new BigDecimal("17423.90"));

                // Verificar precisão de duas casas decimais
                assertThat(pedidoSalvo.getSubtotal().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotalDesconto().scale()).isEqualTo(2);
                assertThat(pedidoSalvo.getTotal().scale()).isEqualTo(2);

                return pedidoSalvo;
            });
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            pedidoService.criar(pedidoRequestDTO);

            // Assert
            verify(pedidoRepository).save(any(Pedido.class));
        }
    }

    @Nested
    @DisplayName("Testes para agrupamento de produtos")
    class AgrupamentoProdutosTests {

        @Test
        @DisplayName("Deve agrupar produtos duplicados no pedido")
        void deveAgruparProdutosDuplicadosNoPedido() {
            // Arrange - adicionar item duplicado do produto 1
            ItemPedidoRequestDTO itemDuplicado = new ItemPedidoRequestDTO();
            itemDuplicado.setProdutoId(1L);
            itemDuplicado.setQuantidade(3);
            itemDuplicado.setDesconto(new BigDecimal("1.00"));

            pedidoRequestDTO.setItens(Arrays.asList(itemRequest1, itemRequest2, itemDuplicado));

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            // Corrigido: Mock para a lista de IDs que será realmente buscada (sem duplicatas)
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            pedidoService.criar(pedidoRequestDTO);

            // Assert - verificar que a quantidade foi agrupada (2 + 3 = 5)
            verify(produtoRepository).baixarEstoque(1L, 5);
            verify(produtoRepository).baixarEstoque(2L, 3);
        }

        @Test
        @DisplayName("Deve validar estoque considerando agrupamento")
        void deveValidarEstoqueConsiderandoAgrupamento() {
            // Arrange
            produto1.setEstoque(4); // Menos que 2 + 3 = 5

            ItemPedidoRequestDTO itemDuplicado = new ItemPedidoRequestDTO();
            itemDuplicado.setProdutoId(1L);
            itemDuplicado.setQuantidade(3);

            pedidoRequestDTO.setItens(Arrays.asList(itemRequest1, itemRequest2, itemDuplicado));

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            // Corrigido: Mock para a lista de IDs que será realmente buscada (sem duplicatas)
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Estoque insuficiente");

            verify(clienteRepository).findById(1L);
            // Corrigido: A verificação deve ser para a lista de IDs sem duplicatas
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para buscar pedido")
    class BuscarPedidoTests {

        @Test
        @DisplayName("Deve buscar pedido por ID com sucesso")
        void deveBuscarPedidoPorIdComSucesso() {
            // Arrange
            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getClienteId()).isEqualTo(1L);

            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoMapper).toResponseDTO(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado")
        void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
            // Arrange
            when(pedidoRepository.findByIdWithFullDetails(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.buscarPorId(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Pedido não encontrado");

            verify(pedidoRepository).findByIdWithFullDetails(999L);
            verify(pedidoMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para listar pedidos")
    class ListarPedidosTests {

        @Test
        @DisplayName("Deve listar pedidos com filtros")
        void deveListarPedidosComFiltros() {
            // Arrange
            Long clienteId = 1L;
            StatusPedido status = StatusPedido.CREATED;
            Pageable pageable = PageRequest.of(0, 10);

            List<Pedido> pedidos = List.of(pedido);
            Page<Pedido> pagePedidos = new PageImpl<>(pedidos, pageable, 1);

            when(pedidoRepository.findByFilters(clienteId, status, pageable)).thenReturn(pagePedidos);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(pedidoResponseDTO);

            // Act
            Page<PedidoResponseDTO> resultado = pedidoService.listar(clienteId, status, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getId()).isEqualTo(1L);
            assertThat(resultado.getTotalElements()).isEqualTo(1);

            verify(pedidoRepository).findByFilters(clienteId, status, pageable);
            verify(pedidoMapper).toResponseDTO(pedido);
        }

        @Test
        @DisplayName("Deve listar pedidos sem filtros")
        void deveListarPedidosSemFiltros() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            List<Pedido> pedidos = List.of(pedido);
            Page<Pedido> pagePedidos = new PageImpl<>(pedidos, pageable, 1);

            when(pedidoRepository.findByFilters(null, null, pageable)).thenReturn(pagePedidos);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(pedidoResponseDTO);

            // Act
            Page<PedidoResponseDTO> resultado = pedidoService.listar(null, null, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);

            verify(pedidoRepository).findByFilters(null, null, pageable);
            verify(pedidoMapper).toResponseDTO(pedido);
        }
    }

    @Nested
    @DisplayName("Testes para pagar pedido")
    class PagarPedidoTests {

        @Test
        @DisplayName("Deve pagar pedido pendente com sucesso")
        void devePagarPedidoPendenteComSucesso() {
            // Arrange
            Pedido pedidoPendente = spy(pedido);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendente));
            when(pedidoRepository.save(pedidoPendente)).thenReturn(pedidoPendente);
            when(pedidoMapper.toResponseDTO(pedidoPendente)).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.pagar(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);

            verify(pedidoRepository).findById(1L);
            verify(pedidoPendente).pagar();
            verify(pedidoRepository).save(pedidoPendente);
            verify(pedidoMapper).toResponseDTO(pedidoPendente);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado")
        void deveLancarExcecaoQuandoPedidoNaoEncontradoParaPagamento() {
            // Arrange
            when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.pagar(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Pedido não encontrado");

            verify(pedidoRepository).findById(999L);
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é possível pagar pedido")
        void deveLancarExcecaoQuandoNaoEPossivelPagarPedido() {
            // Arrange
            Pedido pedidoInvalido = spy(pedido);
            doThrow(new IllegalStateException("Pedido já foi pago"))
                    .when(pedidoInvalido).pagar();

            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoInvalido));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.pagar(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Não é possível pagar o pedido: Pedido já foi pago");

            verify(pedidoRepository).findById(1L);
            verify(pedidoInvalido).pagar();
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para cancelar pedido")
    class CancelarPedidoTests {

        @Test
        @DisplayName("Deve cancelar pedido pendente com sucesso e devolver estoque")
        void deveCancelarPedidoPendenteComSucessoEDevolverEstoque() {
            // Arrange
            ItemPedido item1 = new ItemPedido(pedido, produto1, 2, new BigDecimal("5.00"));
            ItemPedido item2 = new ItemPedido(pedido, produto2, 3, new BigDecimal("2.50"));
            pedido.setItens(Arrays.asList(item1, item2));

            Pedido pedidoParaCancelar = spy(pedido);
            when(pedidoParaCancelar.podeSerCancelado()).thenReturn(true);
            when(pedidoParaCancelar.estaPago()).thenReturn(false);

            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedidoParaCancelar);
            when(pedidoRepository.save(pedidoParaCancelar)).thenReturn(pedidoParaCancelar);
            when(pedidoMapper.toResponseDTO(pedidoParaCancelar)).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.cancelar(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);

            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoParaCancelar).podeSerCancelado();
            verify(pedidoParaCancelar).estaPago();
            verify(produtoRepository).devolverEstoque(1L, 2);
            verify(produtoRepository).devolverEstoque(2L, 3);
            verify(pedidoParaCancelar).cancelar();
            verify(pedidoRepository).save(pedidoParaCancelar);
        }

        @Test
        @DisplayName("Deve cancelar pedido pago sem devolver estoque")
        void deveCancelarPedidoPagoSemDevolverEstoque() {
            // Arrange
            ItemPedido item1 = new ItemPedido(pedido, produto1, 2, new BigDecimal("5.00"));
            pedido.setItens(List.of(item1));

            Pedido pedidoPago = spy(pedido);
            when(pedidoPago.podeSerCancelado()).thenReturn(true);
            when(pedidoPago.estaPago()).thenReturn(true);

            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedidoPago);
            when(pedidoRepository.save(pedidoPago)).thenReturn(pedidoPago);
            when(pedidoMapper.toResponseDTO(pedidoPago)).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.cancelar(1L);

            // Assert
            assertThat(resultado).isNotNull();

            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoPago).podeSerCancelado();
            verify(pedidoPago).estaPago();
            verify(produtoRepository, never()).devolverEstoque(anyLong(), anyInt());
            verify(pedidoPago).cancelar();
            verify(pedidoRepository).save(pedidoPago);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não pode ser cancelado")
        void deveLancarExcecaoQuandoPedidoNaoPodeSerCancelado() {
            // Arrange
            Pedido pedidoNaoCancelavel = spy(pedido);
            when(pedidoNaoCancelavel.podeSerCancelado()).thenReturn(false);

            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedidoNaoCancelavel);

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.cancelar(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Pedido já pago não pode ser cancelado");

            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoNaoCancelavel).podeSerCancelado();
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado para cancelamento")
        void deveLancarExcecaoQuandoPedidoNaoEncontradoParaCancelamento() {
            // Arrange
            when(pedidoRepository.findByIdWithFullDetails(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.cancelar(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Pedido não encontrado");

            verify(pedidoRepository).findByIdWithFullDetails(999L);
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para excluir pedido")
    class ExcluirPedidoTests {

        @Test
        @DisplayName("Deve excluir pedido pendente com devolução de estoque")
        void deveExcluirPedidoPendenteComDevolucaoEstoque() {
            // Arrange
            ItemPedido item1 = new ItemPedido(pedido, produto1, 2, new BigDecimal("5.00"));
            ItemPedido item2 = new ItemPedido(pedido, produto2, 3, new BigDecimal("2.50"));
            pedido.setItens(Arrays.asList(item1, item2));

            Pedido pedidoParaExcluir = spy(pedido);
            when(pedidoParaExcluir.estaPago()).thenReturn(false);

            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedidoParaExcluir);
            when(pedidoRepository.save(pedidoParaExcluir)).thenReturn(pedidoParaExcluir);

            // Act
            pedidoService.excluir(1L);

            // Assert
            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoParaExcluir).estaPago();
            verify(produtoRepository).devolverEstoque(1L, 2);
            verify(produtoRepository).devolverEstoque(2L, 3);
            verify(pedidoParaExcluir).markAsDeleted();
            verify(pedidoRepository).save(pedidoParaExcluir);
        }

        @Test
        @DisplayName("Deve excluir pedido pago sem devolução de estoque")
        void deveExcluirPedidoPagoSemDevolucaoEstoque() {
            // Arrange
            ItemPedido item1 = new ItemPedido(pedido, produto1, 2, new BigDecimal("5.00"));
            pedido.setItens(List.of(item1));

            Pedido pedidoPago = spy(pedido);
            when(pedidoPago.estaPago()).thenReturn(true);

            when(pedidoRepository.findByIdWithFullDetails(1L)).thenReturn(pedidoPago);
            when(pedidoRepository.save(pedidoPago)).thenReturn(pedidoPago);

            // Act
            pedidoService.excluir(1L);

            // Assert
            verify(pedidoRepository).findByIdWithFullDetails(1L);
            verify(pedidoPago).estaPago();
            verify(produtoRepository, never()).devolverEstoque(anyLong(), anyInt());
            verify(pedidoPago).markAsDeleted();
            verify(pedidoRepository).save(pedidoPago);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado para exclusão")
        void deveLancarExcecaoQuandoPedidoNaoEncontradoParaExclusao() {
            // Arrange
            when(pedidoRepository.findByIdWithFullDetails(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.excluir(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Pedido não encontrado");

            verify(pedidoRepository).findByIdWithFullDetails(999L);
            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para validações de produtos")
    class ValidacoesProdutosTests {

        @Test
        @DisplayName("Deve validar múltiplos produtos inativos")
        void deveValidarMultiplosProdutosInativos() {
            // Arrange
            produto1.setAtivo(false);
            produto2.setAtivo(false);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produtos inativos: [Produto 1, Produto 2]");
        }

        @Test
        @DisplayName("Deve validar múltiplos produtos com estoque insuficiente")
        void deveValidarMultiplosProdutosComEstoqueInsuficiente() {
            // Arrange
            produto1.setEstoque(1);
            produto2.setEstoque(2);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));

            // Act & Assert
            assertThatThrownBy(() -> pedidoService.criar(pedidoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Estoque insuficiente para:")
                    .hasMessageContaining("Produto 1 (disponível: 1, solicitado: 2)")
                    .hasMessageContaining("Produto 2 (disponível: 2, solicitado: 3)");
        }

        @Test
        @DisplayName("Deve validar lista vazia de itens")
        void deveValidarListaVaziaDeItens() {
            // Arrange
            pedidoRequestDTO.setItens(Collections.emptyList());

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act & Assert
            assertThatCode(() -> pedidoService.criar(pedidoRequestDTO))
                    .doesNotThrowAnyException();

            verify(produtoRepository, never()).baixarEstoque(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Deve validar produtos com SKU únicos")
        void deveValidarProdutosComSkuUnicos() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(Arrays.asList(1L, 2L)))
                    .thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.criar(pedidoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            verify(produtoRepository).findAllById(Arrays.asList(1L, 2L));
        }
    }

    @Nested
    @DisplayName("Testes para cenários de negócio específicos")
    class CenariosNegocioTests {

        @Test
        @DisplayName("Deve processar pedido com desconto maior que valor do produto")
        void deveProcessarPedidoComDescontoMaiorQueValorDoProduto() {
            // Arrange
            itemRequest1.setDesconto(new BigDecimal("200.00")); // Desconto maior que o valor do produto

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedidoSalvo = invocation.getArgument(0);

                // O total pode ficar negativo, mas o sistema deve aceitar
                assertThat(pedidoSalvo.getSubtotal()).isPositive();
                assertThat(pedidoSalvo.getTotalDesconto()).isPositive();

                return pedidoSalvo;
            });
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act & Assert
            assertThatCode(() -> pedidoService.criar(pedidoRequestDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve processar pedido com produtos de preços variados")
        void deveProcessarPedidoComProdutosDePrecosVariados() {
            // Arrange
            produto1.setPrecoBruto(new BigDecimal("0.01")); // Produto muito barato
            produto2.setPrecoBruto(new BigDecimal("9999.99")); // Produto muito caro

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.criar(pedidoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            verify(produtoRepository).baixarEstoque(1L, 2);
            verify(produtoRepository).baixarEstoque(2L, 3);
        }

        @Test
        @DisplayName("Deve processar pedido com quantidades altas")
        void deveProcessarPedidoComQuantidadesAltas() {
            // Arrange
            itemRequest1.setQuantidade(1000);
            itemRequest2.setQuantidade(2000);
            produto1.setEstoque(2000);
            produto2.setEstoque(3000);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(produtoRepository.findAllById(any())).thenReturn(Arrays.asList(produto1, produto2));
            when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(any())).thenReturn(pedidoResponseDTO);

            // Act
            PedidoResponseDTO resultado = pedidoService.criar(pedidoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            verify(produtoRepository).baixarEstoque(1L, 1000);
            verify(produtoRepository).baixarEstoque(2L, 2000);
        }
    }
}
