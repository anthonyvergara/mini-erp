package com.golden.erp.scheduler;

import com.golden.erp.entity.Produto;
import com.golden.erp.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoSchedulerService.class);

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * Tarefa B: Reabastecimento
     * Executa diariamente às 03:00 e registra em log os produtos com estoque abaixo do mínimo
     */
    @Scheduled(cron = "0 0 3 * * *") // Diariamente às 03:00
    public void verificarReabastecimento() {
        logger.info("Iniciando verificação de reabastecimento de produtos");

        try {
            List<Produto> todosProdutos = produtoRepository.findAll();

            int produtosParaReabastecer = 0;

            for (Produto produto : todosProdutos) {
                if (produto.getEstoque() < produto.getEstoqueMinimo()) {
                    logger.warn("REABASTECIMENTO NECESSÁRIO - Produto: {} (SKU: {}) - Estoque atual: {} - Estoque mínimo: {}",
                               produto.getNome(), produto.getSku(), produto.getEstoque(), produto.getEstoqueMinimo());
                    produtosParaReabastecer++;
                }
            }

            if (produtosParaReabastecer == 0) {
                logger.info("Verificação de reabastecimento concluída. Nenhum produto necessita reabastecimento");
            } else {
                logger.info("Verificação de reabastecimento concluída. {} produtos necessitam reabastecimento", produtosParaReabastecer);
            }

        } catch (Exception e) {
            logger.error("Erro ao verificar reabastecimento de produtos", e);
        }
    }
}
