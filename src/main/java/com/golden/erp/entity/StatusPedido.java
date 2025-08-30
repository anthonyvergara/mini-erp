package com.golden.erp.entity;

public enum StatusPedido {
    CREATED("Criado"),
    PAID("Pago"),
    CANCELLED("Cancelado"),
    LATE("Atrasado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
