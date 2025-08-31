package com.golden.erp.integration.exchange;

import com.golden.erp.dto.exchange.ExchangeRateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "exchange-rate-client", url = "https://api.exchangerate.host")
public interface ExchangeRateClient {

    @GetMapping("/latest")
    ExchangeRateResponseDTO getLatestRates(
        @RequestParam("base") String baseCurrency,
        @RequestParam("symbols") String symbols
    );
}
