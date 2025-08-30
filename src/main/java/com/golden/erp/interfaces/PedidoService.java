package com.golden.erp.interfaces;

import com.golden.erp.dto.pedido.PedidoRequestDTO;
import com.golden.erp.dto.pedido.PedidoResponseDTO;
import com.golden.erp.entity.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {

    PedidoResponseDTO criar(PedidoRequestDTO request);

    PedidoResponseDTO buscarPorId(Long id);

    Page<PedidoResponseDTO> listar(Long clienteId, StatusPedido status, Pageable pageable);

    PedidoResponseDTO pagar(Long id);

    PedidoResponseDTO cancelar(Long id);

    void excluir(Long id);
}
