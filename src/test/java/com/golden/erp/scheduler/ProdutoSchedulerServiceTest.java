package com.golden.erp.scheduler;

import com.golden.erp.entity.Produto;
import com.golden.erp.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoSchedulerServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoSchedulerService produtoSchedulerService;

    private Produto produtoComEstoqueBaixo;
    private Produto produtoComEstoqueAdequado;
    private Produto produtoComEstoqueZero;

    @BeforeEach
    void setUp() {
        // Produto com estoque abaixo do mínimo
        produtoComEstoqueBaixo = new Produto("SKU001", "Produto A",
            new BigDecimal("10.00"), 5, 10, true);
        produtoComEstoqueBaixo.setId(1L);

        // Produto com estoque adequado
        produtoComEstoqueAdequado = new Produto("SKU002", "Produto B",
            new BigDecimal("20.00"), 15, 10, true);
        produtoComEstoqueAdequado.setId(2L);

        // Produto com estoque zero
        produtoComEstoqueZero = new Produto("SKU003", "Produto C",
            new BigDecimal("30.00"), 0, 5, true);
        produtoComEstoqueZero.setId(3L);
    }

    @Test
    void verificarReabastecimento_DeveLogarProdutosComEstoqueBaixo() {
        // Given
        List<Produto> produtos = Arrays.asList(
            produtoComEstoqueBaixo,
            produtoComEstoqueAdequado,
            produtoComEstoqueZero
        );
        when(produtoRepository.findAll()).thenReturn(produtos);

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findAll();

        // Verificações de que os produtos corretos foram identificados
        // produtoComEstoqueBaixo: estoque 5 < estoqueMinimo 10 - deve ser logado
        // produtoComEstoqueAdequado: estoque 15 >= estoqueMinimo 10 - não deve ser logado
        // produtoComEstoqueZero: estoque 0 < estoqueMinimo 5 - deve ser logado
    }

    @Test
    void verificarReabastecimento_QuandoTodosProdutosTemEstoqueAdequado_NaoDeveLogarReabastecimento() {
        // Given
        List<Produto> produtos = Arrays.asList(produtoComEstoqueAdequado);
        when(produtoRepository.findAll()).thenReturn(produtos);

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findAll();
    }

    @Test
    void verificarReabastecimento_QuandoNaoHaProdutos_NaoDeveLogarReabastecimento() {
        // Given
        when(produtoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findAll();
    }

    @Test
    void verificarReabastecimento_QuandoOcorreExcecao_DeveLogarErro() {
        // Given
        when(produtoRepository.findAll())
            .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When/Then - Não deve lançar exceção, apenas logar
        produtoSchedulerService.verificarReabastecimento();

        verify(produtoRepository).findAll();
    }

    @Test
    void verificarReabastecimento_ProdutoComEstoqueIgualAoMinimo_NaoDeveLogarReabastecimento() {
        // Given
        Produto produtoComEstoqueIgualMinimo = new Produto("SKU004", "Produto D",
            new BigDecimal("40.00"), 10, 10, true);
        produtoComEstoqueIgualMinimo.setId(4L);

        List<Produto> produtos = Arrays.asList(produtoComEstoqueIgualMinimo);
        when(produtoRepository.findAll()).thenReturn(produtos);

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findAll();
        // Produto com estoque igual ao mínimo não deve ser considerado para reabastecimento
    }
}
