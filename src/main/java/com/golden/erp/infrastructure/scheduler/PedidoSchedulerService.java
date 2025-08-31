package com.golden.erp.infrastructure.scheduler;

import com.golden.erp.domain.Pedido;
import com.golden.erp.domain.StatusPedido;
import com.golden.erp.infrastructure.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoSchedulerService.class);

    private final PedidoRepository pedidoRepository;

    public PedidoSchedulerService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Tarefa A: Marcar pedidos atrasados
     * Executa a cada hora e marca como LATE os pedidos CREATED com mais de 48 horas
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em milissegundos
    @Transactional
    public void marcarPedidosAtrasados() {
        logger.info("Iniciando verificação de pedidos atrasados");

        try {
            LocalDateTime limitTime = LocalDateTime.now().minusHours(48);

            List<Pedido> pedidosAtrasados = pedidoRepository.findPedidosAtrasados(StatusPedido.CREATED, limitTime);

            long pedidosAtualizados = pedidosAtrasados.stream()
                    .peek(pedido -> {
                        pedido.setStatus(StatusPedido.LATE);
                        logger.debug("Pedido ID {} marcado como atrasado", pedido.getId());
                    })
                    .mapToLong(pedido -> {
                        pedidoRepository.save(pedido);
                        return 1;
                    })
                    .sum();

            logger.info("Verificação de pedidos atrasados concluída. {} pedidos marcados como atrasados", pedidosAtualizados);

        } catch (Exception e) {
            logger.error("Erro ao processar pedidos atrasados", e);
        }
    }
}
