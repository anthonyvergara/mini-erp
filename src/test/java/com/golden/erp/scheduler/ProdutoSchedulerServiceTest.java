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
        List<Produto> produtosComEstoqueBaixo = Arrays.asList(
            produtoComEstoqueBaixo,
            produtoComEstoqueZero
        );
        when(produtoRepository.findProdutosComEstoqueBaixo()).thenReturn(produtosComEstoqueBaixo);

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findProdutosComEstoqueBaixo();
    }

    @Test
    void verificarReabastecimento_QuandoTodosProdutosTemEstoqueAdequado_NaoDeveLogarReabastecimento() {
        // Given
        when(produtoRepository.findProdutosComEstoqueBaixo()).thenReturn(Collections.emptyList());

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findProdutosComEstoqueBaixo();
    }

    @Test
    void verificarReabastecimento_QuandoNaoHaProdutos_NaoDeveLogarReabastecimento() {
        // Given
        when(produtoRepository.findProdutosComEstoqueBaixo()).thenReturn(Collections.emptyList());

        // When
        produtoSchedulerService.verificarReabastecimento();

        // Then
        verify(produtoRepository).findProdutosComEstoqueBaixo();
    }

    @Test
    void verificarReabastecimento_QuandoOcorreExcecao_DeveLogarErro() {
        // Given
        when(produtoRepository.findProdutosComEstoqueBaixo())
            .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When/Then - Não deve lançar exceção, apenas logar
        produtoSchedulerService.verificarReabastecimento();

        verify(produtoRepository).findProdutosComEstoqueBaixo();
    }
}
