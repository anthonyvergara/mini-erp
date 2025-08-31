package com.golden.erp.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public class ExchangeRateResponseDTO {

    @JsonProperty("base")
    private String baseCurrency;

    @JsonProperty("date")
    private String date;

    @JsonProperty("rates")
    private Map<String, BigDecimal> rates;

    public ExchangeRateResponseDTO() {}

    public boolean isSuccess() {
        return rates != null && !rates.isEmpty();
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates;
    }

    public BigDecimal getUsdRate() {
        return rates != null ? rates.get("USD") : null;
    }
}
