package com.golden.erp.controller;

import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import com.golden.erp.interfaces.ProdutoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
@Validated
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = produtoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @PathVariable @NotNull(message = "ID é obrigatório") @Positive(message = "ID deve ser um número positivo") Long id,
            @Valid @RequestBody ProdutoRequestDTO request) {
        ProdutoResponseDTO response = produtoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(
            @PathVariable @NotNull(message = "ID é obrigatório") @Positive(message = "ID deve ser um número positivo") Long id) {
        ProdutoResponseDTO response = produtoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProdutoResponseDTO> response = produtoService.listar(nome, sku, ativo, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable @NotNull(message = "ID é obrigatório") @Positive(message = "ID deve ser um número positivo") Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
