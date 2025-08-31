package com.golden.erp.infrastructure.integration.exchange;

import com.golden.erp.dto.exchange.ExchangeRateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exchange-rate-client", url = "https://api.exchangerate-api.com")
public interface ExchangeRateClient {

    @GetMapping("/v4/latest/{baseCurrency}")
    ExchangeRateResponseDTO getLatestRates(@PathVariable("baseCurrency") String baseCurrency);
}
