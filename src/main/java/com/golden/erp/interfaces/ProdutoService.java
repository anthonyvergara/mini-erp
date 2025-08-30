package com.golden.erp.interfaces;

import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProdutoService {
    ProdutoResponseDTO criar(ProdutoRequestDTO request);

    ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request);

    ProdutoResponseDTO buscarPorId(Long id);

    Page<ProdutoResponseDTO> listar(String nome, String sku, Boolean ativo, Pageable pageable);

    void excluir(Long id);
}
