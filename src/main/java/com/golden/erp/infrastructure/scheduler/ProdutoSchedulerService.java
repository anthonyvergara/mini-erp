package com.golden.erp.infrastructure.scheduler;

import com.golden.erp.domain.Produto;
import com.golden.erp.infrastructure.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoSchedulerService.class);

    private final ProdutoRepository produtoRepository;

    public ProdutoSchedulerService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Scheduled(cron = "0 0 3 * * *") // Diariamente às 03:00
    public void verificarReabastecimento() {
        logger.info("Iniciando verificação de reabastecimento de produtos");

        try {
            List<Produto> produtosParaReabastecer = produtoRepository.findProdutosComEstoqueBaixo();

            produtosParaReabastecer.forEach(produto ->
                logger.warn("REABASTECIMENTO NECESSÁRIO - Produto: {} (SKU: {}) - Estoque atual: {} - Estoque mínimo: {}",
                           produto.getNome(), produto.getSku(), produto.getEstoque(), produto.getEstoqueMinimo())
            );

            if (produtosParaReabastecer.isEmpty()) {
                logger.info("Verificação de reabastecimento concluída. Nenhum produto necessita reabastecimento");
            } else {
                logger.info("Verificação de reabastecimento concluída. {} produtos necessitam reabastecimento", produtosParaReabastecer.size());
            }

        } catch (Exception e) {
            logger.error("Erro ao verificar reabastecimento de produtos", e);
        }
    }
}
