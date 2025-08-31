package com.golden.erp.infrastructure.metrics;

import com.golden.erp.domain.Pedido;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.PostPersist;

import java.time.LocalDateTime;

public class PedidoMetricsListener {
    private static MeterRegistry meterRegistry;

    public PedidoMetricsListener(MeterRegistry meterRegistry) {
        PedidoMetricsListener.meterRegistry = meterRegistry;
    }

    @PostPersist
    public void afterPersist(Pedido pedido) {
        meterRegistry.counter("pedidos.criados").increment();
        String horaAtual = String.valueOf(LocalDateTime.now().getHour());
        meterRegistry.counter("pedidos.criados.por_hora", "hora", horaAtual).increment();
    }
}
