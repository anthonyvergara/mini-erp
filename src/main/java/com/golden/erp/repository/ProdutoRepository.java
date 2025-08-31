package com.golden.erp.repository;

import com.golden.erp.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query(value = "SELECT p FROM Produto p WHERE " +
            "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS text), '%'))) AND " +
            "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', CAST(:sku AS text), '%'))) AND " +
            "(:ativo IS NULL OR p.ativo = :ativo)")
    Page<Produto> findByFilters(@Param("nome") String nome,
                                @Param("sku") String sku,
                                @Param("ativo") Boolean ativo,
                                Pageable pageable);

    @Modifying
    @Query("UPDATE Produto p SET p.estoque = p.estoque - :quantidade WHERE p.id = :produtoId")
    void baixarEstoque(@Param("produtoId") Long produtoId, @Param("quantidade") Integer quantidade);

    @Modifying
    @Query("UPDATE Produto p SET p.estoque = p.estoque + :quantidade WHERE p.id = :produtoId")
    void devolverEstoque(@Param("produtoId") Long produtoId, @Param("quantidade") Integer quantidade);

    @Query("SELECT p FROM Produto p WHERE p.estoque < p.estoqueMinimo AND p.ativo = true")
    List<Produto> findProdutosComEstoqueBaixo();
}
