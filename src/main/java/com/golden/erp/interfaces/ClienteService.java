package com.golden.erp.interfaces;

import com.golden.erp.dto.cliente.ClienteRequestDTO;
import com.golden.erp.dto.cliente.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    ClienteResponseDTO criar(ClienteRequestDTO request);

    ClienteResponseDTO atualizar(Long id, ClienteRequestDTO request);

    ClienteResponseDTO buscarPorId(Long id);

    Page<ClienteResponseDTO> listar(String nome, Pageable pageable);

    void excluir(Long id);
}


