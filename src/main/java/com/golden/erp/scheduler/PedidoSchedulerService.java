package com.golden.erp.scheduler;

import com.golden.erp.entity.Pedido;
import com.golden.erp.entity.StatusPedido;
import com.golden.erp.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoSchedulerService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

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

            // Buscar pedidos CREATED criados há mais de 48 horas
            List<Pedido> pedidosCreated = pedidoRepository.findByStatus(StatusPedido.CREATED);

            int pedidosAtualizados = 0;
            for (Pedido pedido : pedidosCreated) {
                if (pedido.getCreatedAt().isBefore(limitTime)) {
                    pedido.setStatus(StatusPedido.LATE);
                    pedidoRepository.save(pedido);
                    pedidosAtualizados++;
                    logger.debug("Pedido ID {} marcado como atrasado", pedido.getId());
                }
            }

            logger.info("Verificação de pedidos atrasados concluída. {} pedidos marcados como atrasados", pedidosAtualizados);

        } catch (Exception e) {
            logger.error("Erro ao processar pedidos atrasados", e);
        }
    }
}
