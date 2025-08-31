package com.golden.erp.integration.exchange;

import com.golden.erp.dto.exchange.ExchangeRateResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final int CACHE_DURATION_HOURS = 1;

    private final ExchangeRateClient exchangeRateClient;
    private final ConcurrentMap<String, CachedRate> cache = new ConcurrentHashMap<>();

    public ExchangeRateService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal getBrlToUsdRate() {
        String cacheKey = "BRL_USD";
        CachedRate cachedRate = cache.get(cacheKey);

        // Verificar se o cache é válido
        if (cachedRate != null && cachedRate.isValid()) {
            logger.debug("Retornando cotação BRL->USD do cache: {}", cachedRate.getRate());
            return cachedRate.getRate();
        }

        // Buscar nova cotação
        try {
            logger.info("Buscando nova cotação BRL->USD da API externa");
            ExchangeRateResponseDTO response = exchangeRateClient.getLatestRates("BRL", "USD");

            if (response.isSuccess() && response.getUsdRate() != null) {
                BigDecimal usdRate = response.getUsdRate();

                // Armazenar no cache
                cache.put(cacheKey, new CachedRate(usdRate, LocalDateTime.now()));

                logger.info("Nova cotação BRL->USD obtida e armazenada no cache: {}", usdRate);
                return usdRate;
            } else {
                throw new RuntimeException("Resposta inválida da API de câmbio");
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar cotação BRL->USD: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao consultar cotação do câmbio", e);
        }
    }

    public BigDecimal convertBrlToUsd(BigDecimal brlAmount) {
        if (brlAmount == null || brlAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal usdRate = getBrlToUsdRate();
        return brlAmount.multiply(usdRate).setScale(2, RoundingMode.HALF_UP);
    }

    // Limpar cache (útil para testes ou manutenção)
    public void clearCache() {
        cache.clear();
        logger.info("Cache de cotações limpo");
    }

    private static class CachedRate {
        private final BigDecimal rate;
        private final LocalDateTime timestamp;

        public CachedRate(BigDecimal rate, LocalDateTime timestamp) {
            this.rate = rate;
            this.timestamp = timestamp;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public boolean isValid() {
            return LocalDateTime.now().isBefore(timestamp.plusHours(CACHE_DURATION_HOURS));
        }
    }
}
