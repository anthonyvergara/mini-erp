package com.golden.erp.service;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import com.golden.erp.infrastructure.repository.ProdutoRepository;
import com.golden.erp.mapper.ProdutoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoServiceImpl - Testes Unitários")
class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    private ProdutoRequestDTO produtoRequestDTO;
    private Produto produto;
    private ProdutoResponseDTO produtoResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup dos dados de teste
        produtoRequestDTO = new ProdutoRequestDTO();
        produtoRequestDTO.setSku("PROD001");
        produtoRequestDTO.setNome("Produto Teste");
        produtoRequestDTO.setPrecoBruto(new BigDecimal("99.99"));
        produtoRequestDTO.setEstoque(100);
        produtoRequestDTO.setEstoqueMinimo(10);
        produtoRequestDTO.setAtivo(true);

        produto = new Produto();
        produto.setId(1L);
        produto.setSku("PROD001");
        produto.setNome("Produto Teste");
        produto.setPrecoBruto(new BigDecimal("99.99"));
        produto.setEstoque(100);
        produto.setEstoqueMinimo(10);
        produto.setAtivo(true);

        produtoResponseDTO = new ProdutoResponseDTO();
        produtoResponseDTO.setId(1L);
        produtoResponseDTO.setSku("PROD001");
        produtoResponseDTO.setNome("Produto Teste");
        produtoResponseDTO.setPrecoBruto(new BigDecimal("99.99"));
        produtoResponseDTO.setEstoque(100);
        produtoResponseDTO.setEstoqueMinimo(10);
        produtoResponseDTO.setAtivo(true);
    }

    @Nested
    @DisplayName("Testes para criação de produto")
    class CriarProdutoTests {

        @Test
        @DisplayName("Deve criar produto com sucesso")
        void deveCriarProdutoComSucesso() {
            // Arrange
            when(produtoRepository.existsBySku(produtoRequestDTO.getSku())).thenReturn(false);
            when(produtoMapper.toEntity(produtoRequestDTO)).thenReturn(produto);
            when(produtoRepository.save(produto)).thenReturn(produto);
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            ProdutoResponseDTO resultado = produtoService.criar(produtoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getSku()).isEqualTo("PROD001");
            assertThat(resultado.getNome()).isEqualTo("Produto Teste");
            assertThat(resultado.getPrecoBruto()).isEqualTo(new BigDecimal("99.99"));
            assertThat(resultado.getEstoque()).isEqualTo(100);
            assertThat(resultado.getEstoqueMinimo()).isEqualTo(10);
            assertThat(resultado.getAtivo()).isTrue();

            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoMapper).toEntity(produtoRequestDTO);
            verify(produtoRepository).save(produto);
            verify(produtoMapper).toResponseDTO(produto);
        }

        @Test
        @DisplayName("Deve lançar exceção quando SKU já existe")
        void deveLancarExcecaoQuandoSkuJaExiste() {
            // Arrange
            when(produtoRepository.existsBySku(produtoRequestDTO.getSku())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> produtoService.criar(produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("SKU já está em uso");

            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque mínimo é maior que estoque atual")
        void deveLancarExcecaoQuandoEstoqueMinimoMaiorQueAtual() {
            // Arrange
            produtoRequestDTO.setEstoque(5);
            produtoRequestDTO.setEstoqueMinimo(10);
            when(produtoRepository.existsBySku(any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> produtoService.criar(produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Estoque mínimo não pode ser maior que o estoque atual");

            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar produto mesmo com estoque baixo (apenas warning)")
        void deveCriarProdutoComEstoqueBaixo() {
            // Arrange
            produtoRequestDTO.setEstoque(5);
            produtoRequestDTO.setEstoqueMinimo(5);

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();

            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository).save(produto);
        }

        @Test
        @DisplayName("Deve criar produto inativo (apenas warning)")
        void deveCriarProdutoInativo() {
            // Arrange
            produtoRequestDTO.setAtivo(false);

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();

            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository).save(produto);
        }
    }

    @Nested
    @DisplayName("Testes para atualização de produto")
    class AtualizarProdutoTests {

        @Test
        @DisplayName("Deve atualizar produto com sucesso")
        void deveAtualizarProdutoComSucesso() {
            // Arrange
            Long produtoId = 1L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoRepository.existsBySku(produtoRequestDTO.getSku())).thenReturn(true);
            when(produtoRepository.findBySku(produtoRequestDTO.getSku())).thenReturn(Optional.of(produto));
            when(produtoRepository.save(produto)).thenReturn(produto);
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            ProdutoResponseDTO resultado = produtoService.atualizar(produtoId, produtoRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(produtoId);

            verify(produtoRepository).findById(produtoId);
            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository).findBySku(produtoRequestDTO.getSku());
            verify(produtoMapper).updateEntityFromDTO(produtoRequestDTO, produto);
            verify(produtoRepository).save(produto);
            verify(produtoMapper).toResponseDTO(produto);
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado")
        void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
            // Arrange
            Long produtoId = 999L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> produtoService.atualizar(produtoId, produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produto não encontrado com ID: " + produtoId);

            verify(produtoRepository).findById(produtoId);
            verify(produtoRepository, never()).existsBySku(any());
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando SKU pertence a outro produto")
        void deveLancarExcecaoQuandoSkuPertenceAOutroProduto() {
            // Arrange
            Long produtoId = 1L;
            Produto outroProduto = new Produto();
            outroProduto.setId(2L);
            outroProduto.setSku(produtoRequestDTO.getSku());

            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoRepository.existsBySku(produtoRequestDTO.getSku())).thenReturn(true);
            when(produtoRepository.findBySku(produtoRequestDTO.getSku())).thenReturn(Optional.of(outroProduto));

            // Act & Assert
            assertThatThrownBy(() -> produtoService.atualizar(produtoId, produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("SKU já está em uso por outro produto");

            verify(produtoRepository).findById(produtoId);
            verify(produtoRepository).existsBySku(produtoRequestDTO.getSku());
            verify(produtoRepository).findBySku(produtoRequestDTO.getSku());
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque mínimo é maior que estoque atual")
        void deveLancarExcecaoQuandoEstoqueMinimoMaiorQueAtualNaAtualizacao() {
            // Arrange
            Long produtoId = 1L;
            produtoRequestDTO.setEstoque(3);
            produtoRequestDTO.setEstoqueMinimo(10);

            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoRepository.existsBySku(any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> produtoService.atualizar(produtoId, produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Estoque mínimo não pode ser maior que o estoque atual");

            verify(produtoRepository).findById(produtoId);
            verify(produtoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve permitir atualização com mesmo SKU do próprio produto")
        void devePermitirAtualizacaoComMesmoSkuDoProproProduto() {
            // Arrange
            Long produtoId = 1L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoRepository.existsBySku(produtoRequestDTO.getSku())).thenReturn(true);
            when(produtoRepository.findBySku(produtoRequestDTO.getSku())).thenReturn(Optional.of(produto));
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.atualizar(produtoId, produtoRequestDTO))
                    .doesNotThrowAnyException();

            verify(produtoRepository).findBySku(produtoRequestDTO.getSku());
        }
    }

    @Nested
    @DisplayName("Testes para busca de produto")
    class BuscarProdutoTests {

        @Test
        @DisplayName("Deve buscar produto por ID com sucesso")
        void deveBuscarProdutoPorIdComSucesso() {
            // Arrange
            Long produtoId = 1L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            ProdutoResponseDTO resultado = produtoService.buscarPorId(produtoId);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(produtoId);
            assertThat(resultado.getSku()).isEqualTo("PROD001");
            assertThat(resultado.getNome()).isEqualTo("Produto Teste");

            verify(produtoRepository).findById(produtoId);
            verify(produtoMapper).toResponseDTO(produto);
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado")
        void deveLancarExcecaoQuandoProdutoNaoEncontradoNaBusca() {
            // Arrange
            Long produtoId = 999L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> produtoService.buscarPorId(produtoId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produto não encontrado com ID: " + produtoId);

            verify(produtoRepository).findById(produtoId);
            verify(produtoMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para listagem de produtos")
    class ListarProdutosTests {

        @Test
        @DisplayName("Deve listar produtos com todos os filtros")
        void deveListarProdutosComTodosFiltros() {
            // Arrange
            String nome = "Produto";
            String sku = "PROD";
            Boolean ativo = true;
            Pageable pageable = PageRequest.of(0, 10);
            List<Produto> produtos = List.of(produto);
            Page<Produto> pageProdutos = new PageImpl<>(produtos, pageable, 1);

            when(produtoRepository.findByFilters(nome, sku, ativo, pageable)).thenReturn(pageProdutos);
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            Page<ProdutoResponseDTO> resultado = produtoService.listar(nome, sku, ativo, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Produto Teste");
            assertThat(resultado.getTotalElements()).isEqualTo(1);

            verify(produtoRepository).findByFilters(nome, sku, ativo, pageable);
            verify(produtoMapper).toResponseDTO(produto);
        }

        @Test
        @DisplayName("Deve listar produtos sem filtros")
        void deveListarProdutosSemFiltros() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Produto> produtos = List.of(produto);
            Page<Produto> pageProdutos = new PageImpl<>(produtos, pageable, 1);

            when(produtoRepository.findByFilters(null, null, null, pageable)).thenReturn(pageProdutos);
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            Page<ProdutoResponseDTO> resultado = produtoService.listar(null, null, null, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);

            verify(produtoRepository).findByFilters(null, null, null, pageable);
            verify(produtoMapper).toResponseDTO(produto);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há produtos")
        void deveRetornarPaginaVaziaQuandoNaoHaProdutos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Produto> pageVazia = new PageImpl<>(List.of(), pageable, 0);

            when(produtoRepository.findByFilters(any(), any(), any(), eq(pageable))).thenReturn(pageVazia);

            // Act
            Page<ProdutoResponseDTO> resultado = produtoService.listar("Inexistente", null, null, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).isEmpty();
            assertThat(resultado.getTotalElements()).isEqualTo(0);

            verify(produtoRepository).findByFilters("Inexistente", null, null, pageable);
            verify(produtoMapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Deve listar apenas produtos ativos")
        void deveListarApenasProdutosAtivos() {
            // Arrange
            Boolean ativo = true;
            Pageable pageable = PageRequest.of(0, 10);
            List<Produto> produtos = List.of(produto);
            Page<Produto> pageProdutos = new PageImpl<>(produtos, pageable, 1);

            when(produtoRepository.findByFilters(null, null, ativo, pageable)).thenReturn(pageProdutos);
            when(produtoMapper.toResponseDTO(produto)).thenReturn(produtoResponseDTO);

            // Act
            Page<ProdutoResponseDTO> resultado = produtoService.listar(null, null, ativo, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getAtivo()).isTrue();

            verify(produtoRepository).findByFilters(null, null, ativo, pageable);
        }
    }

    @Nested
    @DisplayName("Testes para exclusão de produto")
    class ExcluirProdutoTests {

        @Test
        @DisplayName("Deve excluir produto com sucesso (soft delete)")
        void deveExcluirProdutoComSucesso() {
            // Arrange
            Long produtoId = 1L;
            Produto produtoParaExcluir = spy(produto);
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produtoParaExcluir));
            when(produtoRepository.save(produtoParaExcluir)).thenReturn(produtoParaExcluir);

            // Act
            produtoService.excluir(produtoId);

            // Assert
            verify(produtoRepository).findById(produtoId);
            verify(produtoParaExcluir).markAsDeleted();
            verify(produtoRepository).save(produtoParaExcluir);
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado para exclusão")
        void deveLancarExcecaoQuandoProdutoNaoEncontradoParaExclusao() {
            // Arrange
            Long produtoId = 999L;
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> produtoService.excluir(produtoId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Produto não encontrado com ID: " + produtoId);

            verify(produtoRepository).findById(produtoId);
            verify(produtoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para validações de negócio")
    class ValidacoesNegocioTests {

        @Test
        @DisplayName("Deve validar estoque zerado com estoque mínimo zerado")
        void deveValidarEstoqueZeradoComEstoqueMinimoZerado() {
            // Arrange
            produtoRequestDTO.setEstoque(0);
            produtoRequestDTO.setEstoqueMinimo(0);

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve validar preço com valores decimais")
        void deveValidarPrecoComValoresDecimais() {
            // Arrange
            produtoRequestDTO.setPrecoBruto(new BigDecimal("29.95"));

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve capturar argumentos da validação de SKU")
        void deveCapturaArgumentosDaValidacaoSku() {
            // Arrange
            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act
            produtoService.criar(produtoRequestDTO);

            // Assert
            ArgumentCaptor<String> skuCaptor = ArgumentCaptor.forClass(String.class);
            verify(produtoRepository).existsBySku(skuCaptor.capture());
            assertThat(skuCaptor.getValue()).isEqualTo("PROD001");
        }
    }

    @Nested
    @DisplayName("Testes para cenários limítrofes")
    class CenariosLimitrofesTests {

        @Test
        @DisplayName("Deve aceitar estoque igual ao estoque mínimo")
        void deveAceitarEstoqueIgualAoEstoqueMinimo() {
            // Arrange
            produtoRequestDTO.setEstoque(10);
            produtoRequestDTO.setEstoqueMinimo(10);

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve rejeitar estoque menor que estoque mínimo")
        void deveRejeitarEstoqueMenorQueEstoqueMinimo() {
            // Arrange
            produtoRequestDTO.setEstoque(5);
            produtoRequestDTO.setEstoqueMinimo(10);

            when(produtoRepository.existsBySku(any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> produtoService.criar(produtoRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Estoque mínimo não pode ser maior que o estoque atual");
        }

        @Test
        @DisplayName("Deve aceitar estoque maior que estoque mínimo")
        void deveAceitarEstoqueMaiorQueEstoqueMinimo() {
            // Arrange
            produtoRequestDTO.setEstoque(20);
            produtoRequestDTO.setEstoqueMinimo(10);

            when(produtoRepository.existsBySku(any())).thenReturn(false);
            when(produtoMapper.toEntity(any())).thenReturn(produto);
            when(produtoRepository.save(any())).thenReturn(produto);
            when(produtoMapper.toResponseDTO(any())).thenReturn(produtoResponseDTO);

            // Act & Assert
            assertThatCode(() -> produtoService.criar(produtoRequestDTO))
                    .doesNotThrowAnyException();
        }
    }
}
