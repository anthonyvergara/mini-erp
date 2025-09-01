package com.golden.erp.service;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import com.golden.erp.exception.ConflictException;
import com.golden.erp.exception.EntityNotFoundException;
import com.golden.erp.interfaces.ProdutoService;
import com.golden.erp.infrastructure.repository.ProdutoRepository;
import com.golden.erp.mapper.ProdutoMapper;
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
    private final ProdutoMapper produtoMapper;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO request) {
        logger.info("Criando produto: {}", request.getNome());

        validarUnicidadeSku(request.getSku(), null);
        validarRegrasNegocio(request);

        Produto produto = produtoMapper.toEntity(request);

        Produto produtoSalvo = produtoRepository.save(produto);
        logger.info("Produto criado com sucesso. ID: {} - SKU: {}", produtoSalvo.getId(), produtoSalvo.getSku());

        return produtoMapper.toResponseDTO(produtoSalvo);
    }

    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request) {
        logger.info("Atualizando produto ID: {}", id);

        Produto produto = buscarProdutoPorId(id);

        validarUnicidadeSku(request.getSku(), id);
        validarRegrasNegocio(request);

        produtoMapper.updateEntityFromDTO(request, produto);

        Produto produtoAtualizado = produtoRepository.save(produto);
        logger.info("Produto atualizado com sucesso. ID: {} - SKU: {}", produtoAtualizado.getId(), produtoAtualizado.getSku());

        return produtoMapper.toResponseDTO(produtoAtualizado);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        logger.info("Buscando produto por ID: {}", id);
        Produto produto = buscarProdutoPorId(id);
        return produtoMapper.toResponseDTO(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, String sku, Boolean ativo, Pageable pageable) {
        logger.info("Listando produtos com filtros - nome: {}, sku: {}, ativo: {}", nome, sku, ativo);
        Page<Produto> produtos = produtoRepository.findByFilters(nome, sku, ativo, pageable);
        return produtos.map(produtoMapper::toResponseDTO);
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
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));
    }

    private void validarUnicidadeSku(String sku, Long idExcluir) {
        boolean skuJaExiste = produtoRepository.existsBySku(sku);

        if (skuJaExiste) {
            if (idExcluir != null) {
                Produto produtoComSku = produtoRepository.findBySku(sku).orElse(null);
                if (produtoComSku != null && !produtoComSku.getId().equals(idExcluir)) {
                    throw new ConflictException("SKU já está em uso por outro produto");
                }
            } else {
                throw new ConflictException("SKU já está em uso");
            }
        }
    }

    private void validarRegrasNegocio(ProdutoRequestDTO request) {
        if (request.getEstoqueMinimo() > request.getEstoque()) {
            logger.warn("Tentativa de criar/atualizar produto com estoque mínimo ({}) maior que estoque atual ({})",
                       request.getEstoqueMinimo(), request.getEstoque());
            throw new RuntimeException("Estoque mínimo não pode ser maior que o estoque atual");
        }

        if (request.getEstoque() <= request.getEstoqueMinimo() && request.getEstoque() > 0) {
            logger.warn("Produto {} será criado/atualizado com estoque ({}) abaixo ou igual ao mínimo ({})",
                       request.getNome(), request.getEstoque(), request.getEstoqueMinimo());
        }

        if (!request.getAtivo()) {
            logger.warn("Produto {} será criado/atualizado como inativo", request.getNome());
        }
    }
}
