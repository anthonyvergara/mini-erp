package com.golden.erp.scheduler;

import com.golden.erp.entity.Cliente;
import com.golden.erp.entity.Pedido;
import com.golden.erp.entity.StatusPedido;
import com.golden.erp.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoSchedulerServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoSchedulerService pedidoSchedulerService;

    private Pedido pedidoAtrasado;
    private Pedido pedidoRecente;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Cliente Teste");

        // Pedido criado há 50 horas (deve ser marcado como atrasado)
        pedidoAtrasado = new Pedido(cliente);
        pedidoAtrasado.setId(1L);
        pedidoAtrasado.setStatus(StatusPedido.CREATED);
        // Simulando data de criação há 50 horas
        LocalDateTime dataAtrasada = LocalDateTime.now().minusHours(50);
        ReflectionTestUtils.setField(pedidoAtrasado, "createdAt", dataAtrasada);

        // Pedido criado há 30 horas (não deve ser marcado como atrasado)
        pedidoRecente = new Pedido(cliente);
        pedidoRecente.setId(2L);
        pedidoRecente.setStatus(StatusPedido.CREATED);
        // Simulando data de criação há 30 horas
        LocalDateTime dataRecente = LocalDateTime.now().minusHours(30);
        ReflectionTestUtils.setField(pedidoRecente, "createdAt", dataRecente);
    }

    @Test
    void marcarPedidosAtrasados_DeveMarcarApenasPedidosComMaisDe48Horas() {
        // Given
        List<Pedido> pedidosCreated = Arrays.asList(pedidoAtrasado, pedidoRecente);
        when(pedidoRepository.findByStatus(StatusPedido.CREATED)).thenReturn(pedidosCreated);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtrasado);

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findByStatus(StatusPedido.CREATED);
        verify(pedidoRepository, times(1)).save(pedidoAtrasado);
        verify(pedidoRepository, never()).save(pedidoRecente);

        // Verificar se o status foi alterado
        assert pedidoAtrasado.getStatus() == StatusPedido.LATE;
        assert pedidoRecente.getStatus() == StatusPedido.CREATED;
    }

    @Test
    void marcarPedidosAtrasados_QuandoNaoHaPedidosCreated_NaoDeveMarcarNenhum() {
        // Given
        when(pedidoRepository.findByStatus(StatusPedido.CREATED)).thenReturn(Collections.emptyList());

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findByStatus(StatusPedido.CREATED);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void marcarPedidosAtrasados_QuandoTodosPedidosSaoRecentes_NaoDeveMarcarNenhum() {
        // Given
        List<Pedido> pedidosRecentes = Arrays.asList(pedidoRecente);
        when(pedidoRepository.findByStatus(StatusPedido.CREATED)).thenReturn(pedidosRecentes);

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findByStatus(StatusPedido.CREATED);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        assert pedidoRecente.getStatus() == StatusPedido.CREATED;
    }

    @Test
    void marcarPedidosAtrasados_QuandoOcorreExcecao_DeveLogarErro() {
        // Given
        when(pedidoRepository.findByStatus(StatusPedido.CREATED))
            .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When/Then - Não deve lançar exceção, apenas logar
        pedidoSchedulerService.marcarPedidosAtrasados();

        verify(pedidoRepository).findByStatus(StatusPedido.CREATED);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}
