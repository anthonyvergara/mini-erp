package com.golden.erp.dto.produto;

import java.math.BigDecimal;

public class ProdutoResponseDTO {

    private Long id;
    private String sku;
    private String nome;
    private BigDecimal precoBruto;
    private Integer estoque;
    private Integer estoqueMinimo;
    private Boolean ativo;

    public ProdutoResponseDTO() {}

    public ProdutoResponseDTO(Long id, String sku, String nome, BigDecimal precoBruto,
                             Integer estoque, Integer estoqueMinimo, Boolean ativo) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.precoBruto = precoBruto;
        this.estoque = estoque;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoBruto() {
        return precoBruto;
    }

    public void setPrecoBruto(BigDecimal precoBruto) {
        this.precoBruto = precoBruto;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}

