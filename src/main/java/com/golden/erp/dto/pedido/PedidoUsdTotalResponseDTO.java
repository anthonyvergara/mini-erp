package com.golden.erp.dto.pedido;

import java.math.BigDecimal;

public class PedidoUsdTotalResponseDTO {

    private Long pedidoId;
    private BigDecimal totalBrl;
    private BigDecimal totalUsd;
    private BigDecimal exchangeRate;
    private String baseCurrency;
    private String targetCurrency;

    public PedidoUsdTotalResponseDTO() {}

    public PedidoUsdTotalResponseDTO(Long pedidoId, BigDecimal totalBrl, BigDecimal totalUsd, BigDecimal exchangeRate) {
        this.pedidoId = pedidoId;
        this.totalBrl = totalBrl;
        this.totalUsd = totalUsd;
        this.exchangeRate = exchangeRate;
        this.baseCurrency = "BRL";
        this.targetCurrency = "USD";
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getTotalBrl() {
        return totalBrl;
    }

    public void setTotalBrl(BigDecimal totalBrl) {
        this.totalBrl = totalBrl;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(BigDecimal totalUsd) {
        this.totalUsd = totalUsd;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }
}
