package com.golden.erp.scheduler;

import com.golden.erp.domain.Cliente;
import com.golden.erp.domain.Pedido;
import com.golden.erp.domain.StatusPedido;
import com.golden.erp.infrastructure.repository.PedidoRepository;
import com.golden.erp.infrastructure.scheduler.PedidoSchedulerService;
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
import static org.mockito.ArgumentMatchers.eq;
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
        List<Pedido> pedidosAtrasados = Arrays.asList(pedidoAtrasado);
        when(pedidoRepository.findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class)))
            .thenReturn(pedidosAtrasados);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtrasado);

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class));
        verify(pedidoRepository, times(1)).save(pedidoAtrasado);

        // Verificar se o status foi alterado
        assert pedidoAtrasado.getStatus() == StatusPedido.LATE;
    }

    @Test
    void marcarPedidosAtrasados_QuandoNaoHaPedidosAtrasados_NaoDeveMarcarNenhum() {
        // Given
        when(pedidoRepository.findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void marcarPedidosAtrasados_QuandoOcorreExcecao_DeveLogarErro() {
        // Given
        when(pedidoRepository.findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When/Then - Não deve lançar exceção, apenas logar
        pedidoSchedulerService.marcarPedidosAtrasados();

        verify(pedidoRepository).findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void marcarPedidosAtrasados_ComMultiplosPedidos_DeveMarcarTodos() {
        // Given
        Pedido outroPedidoAtrasado = new Pedido(cliente);
        outroPedidoAtrasado.setId(3L);
        outroPedidoAtrasado.setStatus(StatusPedido.CREATED);

        List<Pedido> pedidosAtrasados = Arrays.asList(pedidoAtrasado, outroPedidoAtrasado);
        when(pedidoRepository.findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class)))
            .thenReturn(pedidosAtrasados);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoAtrasado);

        // When
        pedidoSchedulerService.marcarPedidosAtrasados();

        // Then
        verify(pedidoRepository).findPedidosAtrasados(eq(StatusPedido.CREATED), any(LocalDateTime.class));
        verify(pedidoRepository, times(2)).save(any(Pedido.class));

        // Verificar se ambos os status foram alterados
        assert pedidoAtrasado.getStatus() == StatusPedido.LATE;
        assert outroPedidoAtrasado.getStatus() == StatusPedido.LATE;
    }
}
