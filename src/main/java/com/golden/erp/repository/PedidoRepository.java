package com.golden.erp.repository;

import com.golden.erp.entity.Pedido;
import com.golden.erp.entity.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE (:clienteId IS NULL OR p.cliente.id = :clienteId) AND (:status IS NULL OR p.status = :status)")
    Page<Pedido> findByFilters(@Param("clienteId") Long clienteId, @Param("status") StatusPedido status, Pageable pageable);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByStatus(StatusPedido status);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.id = :id")
    Pedido findByIdWithCliente(@Param("id") Long id);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente JOIN FETCH p.itens i JOIN FETCH i.produto WHERE p.id = :id")
    Pedido findByIdWithFullDetails(@Param("id") Long id);
}
