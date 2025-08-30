package com.golden.erp.service;

import com.golden.erp.entity.Produto;
import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import com.golden.erp.interfaces.ProdutoService;
import com.golden.erp.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoServiceImpl.class);

    private final ProdutoRepository produtoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO request) {
        logger.info("Criando produto: {}", request.getNome());

        validarUnicidadeSku(request.getSku(), null);
        validarRegrasNegocio(request);

        Produto produto = new Produto();
        produto.setSku(request.getSku());
        produto.setNome(request.getNome());
        produto.setPrecoBruto(request.getPrecoBruto());
        produto.setEstoque(request.getEstoque());
        produto.setEstoqueMinimo(request.getEstoqueMinimo());
        produto.setAtivo(request.getAtivo());

        Produto produtoSalvo = produtoRepository.save(produto);
        logger.info("Produto criado com sucesso. ID: {} - SKU: {}", produtoSalvo.getId(), produtoSalvo.getSku());

        return converterParaResponseDTO(produtoSalvo);
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request) {
        logger.info("Atualizando produto ID: {}", id);

        Produto produto = buscarProdutoPorId(id);

        validarUnicidadeSku(request.getSku(), id);
        validarRegrasNegocio(request);

        produto.setSku(request.getSku());
        produto.setNome(request.getNome());
        produto.setPrecoBruto(request.getPrecoBruto());
        produto.setEstoque(request.getEstoque());
        produto.setEstoqueMinimo(request.getEstoqueMinimo());
        produto.setAtivo(request.getAtivo());

        Produto produtoAtualizado = produtoRepository.save(produto);
        logger.info("Produto atualizado com sucesso. ID: {} - SKU: {}", produtoAtualizado.getId(), produtoAtualizado.getSku());

        return converterParaResponseDTO(produtoAtualizado);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        logger.info("Buscando produto por ID: {}", id);
        Produto produto = buscarProdutoPorId(id);
        return converterParaResponseDTO(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, String sku, Boolean ativo, Pageable pageable) {
        logger.info("Listando produtos com filtros - nome: {}, sku: {}, ativo: {}", nome, sku, ativo);
        Page<Produto> produtos = produtoRepository.findByFilters(nome, sku, ativo, pageable);
        return produtos.map(this::converterParaResponseDTO);
    }

    public void excluir(Long id) {
        logger.info("Executando soft delete do produto ID: {}", id);
        Produto produto = buscarProdutoPorId(id);
        produto.markAsDeleted();
        produtoRepository.save(produto);
        logger.info("Produto marcado como excluído com sucesso. ID: {} - SKU: {}", produto.getId(), produto.getSku());
    }

    private Produto buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }

    private void validarUnicidadeSku(String sku, Long idExcluir) {
        boolean skuJaExiste = produtoRepository.existsBySku(sku);

        if (skuJaExiste) {
            // Se é atualização, verifica se o SKU pertence ao próprio produto
            if (idExcluir != null) {
                Produto produtoComSku = produtoRepository.findBySku(sku).orElse(null);
                if (produtoComSku != null && !produtoComSku.getId().equals(idExcluir)) {
                    throw new RuntimeException("SKU já está em uso por outro produto");
                }
            } else {
                throw new RuntimeException("SKU já está em uso");
            }
        }
    }

    private void validarRegrasNegocio(ProdutoRequestDTO request) {
        // Validar se estoque mínimo não é maior que estoque atual
        if (request.getEstoqueMinimo() > request.getEstoque()) {
            logger.warn("Tentativa de criar/atualizar produto com estoque mínimo ({}) maior que estoque atual ({})",
                       request.getEstoqueMinimo(), request.getEstoque());
            throw new RuntimeException("Estoque mínimo não pode ser maior que o estoque atual");
        }

        // Log de aviso se estoque está abaixo do mínimo
        if (request.getEstoque() <= request.getEstoqueMinimo() && request.getEstoque() > 0) {
            logger.warn("Produto {} será criado/atualizado com estoque ({}) abaixo ou igual ao mínimo ({})",
                       request.getNome(), request.getEstoque(), request.getEstoqueMinimo());
        }

        // Log de aviso se produto está sendo criado inativo
        if (!request.getAtivo()) {
            logger.warn("Produto {} será criado/atualizado como inativo", request.getNome());
        }
    }

    private ProdutoResponseDTO converterParaResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getPrecoBruto(),
                produto.getEstoque(),
                produto.getEstoqueMinimo(),
                produto.getAtivo()
        );
    }
}
