package com.golden.erp.controller;

import com.golden.erp.dto.pedido.PedidoRequestDTO;
import com.golden.erp.dto.pedido.PedidoResponseDTO;
import com.golden.erp.dto.pedido.PedidoUsdTotalResponseDTO;
import com.golden.erp.entity.StatusPedido;
import com.golden.erp.interfaces.PedidoService;
import com.golden.erp.integration.exchange.ExchangeRateService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ExchangeRateService exchangeRateService;

    public PedidoController(PedidoService pedidoService, ExchangeRateService exchangeRateService) {
        this.pedidoService = pedidoService;
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO response = pedidoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PedidoResponseDTO> response = pedidoService.listar(clienteId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<PedidoResponseDTO> pagar(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.pagar(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.cancelar(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/usd-total")
    public ResponseEntity<PedidoUsdTotalResponseDTO> obterTotalEmUsd(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);

        java.math.BigDecimal exchangeRate = exchangeRateService.getBrlToUsdRate();
        java.math.BigDecimal totalUsd = exchangeRateService.convertBrlToUsd(pedido.getTotal());

        PedidoUsdTotalResponseDTO response = new PedidoUsdTotalResponseDTO(
                pedido.getId(),
                pedido.getTotal(),
                totalUsd,
                exchangeRate
        );

        return ResponseEntity.ok(response);
    }
}
