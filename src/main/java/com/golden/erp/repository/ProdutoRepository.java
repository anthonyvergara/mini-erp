package com.golden.erp.repository;

import com.golden.erp.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Produto p WHERE " +
           "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:ativo IS NULL OR p.ativo = :ativo)")
    Page<Produto> findByFilters(@Param("nome") String nome,
                               @Param("sku") String sku,
                               @Param("ativo") Boolean ativo,
                               Pageable pageable);
}
