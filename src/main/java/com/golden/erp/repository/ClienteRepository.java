package com.golden.erp.repository;

import com.golden.erp.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    @Query(value = "SELECT * FROM clientes c WHERE " +
           "(c.deleted_at IS NULL) AND " +
           "(:nome IS NULL OR CAST(c.nome AS VARCHAR) ILIKE CONCAT('%', :nome, '%'))",
           nativeQuery = true)
    Page<Cliente> findByFilters(@Param("nome") String nome, Pageable pageable);
}
