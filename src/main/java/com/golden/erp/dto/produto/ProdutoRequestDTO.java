package com.golden.erp.dto.produto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProdutoRequestDTO {

    @NotBlank(message = "SKU é obrigatório")
    @Size(max = 50, message = "SKU deve ter no máximo 50 caracteres")
    private String sku;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String nome;

    @NotNull(message = "Preço bruto é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço bruto deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Preço bruto deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal precoBruto;

    @NotNull(message = "Estoque é obrigatório")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private Integer estoque;

    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    public ProdutoRequestDTO() {}

    public ProdutoRequestDTO(String sku, String nome, BigDecimal precoBruto, Integer estoque,
                            Integer estoqueMinimo, Boolean ativo) {
        this.sku = sku;
        this.nome = nome;
        this.precoBruto = precoBruto;
        this.estoque = estoque;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = ativo;
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
