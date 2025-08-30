package com.golden.erp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@SQLRestriction("deleted_at IS NULL")
public class Pedido extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.CREATED;

    @NotNull(message = "Subtotal é obrigatório")
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @NotNull(message = "Total de descontos é obrigatório")
    @DecimalMin("0.0")
    @Column(name = "total_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDesconto = BigDecimal.ZERO;

    @NotNull(message = "Total é obrigatório")
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    // Construtores
    public Pedido() {}

    public Pedido(Cliente cliente) {
        this.cliente = cliente;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalDesconto() {
        return totalDesconto;
    }

    public void setTotalDesconto(BigDecimal totalDesconto) {
        this.totalDesconto = totalDesconto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    // Métodos de negócio
    public void pagar() {
        if (this.status != StatusPedido.CREATED) {
            throw new IllegalStateException("Pedido só pode ser pago se estiver no status CREATED");
        }
        this.status = StatusPedido.PAID;
    }

    public void cancelar() {
        if (this.status == StatusPedido.PAID) {
            throw new IllegalStateException("Pedido já pago não pode ser cancelado");
        }
        this.status = StatusPedido.CANCELLED;
    }

    public boolean podeSerCancelado() {
        return this.status != StatusPedido.PAID;
    }

    public boolean estaPago() {
        return this.status == StatusPedido.PAID;
    }
}
