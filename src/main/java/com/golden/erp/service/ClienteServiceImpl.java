package com.golden.erp.service;

import com.golden.erp.domain.Cliente;
import com.golden.erp.dto.cliente.ClienteRequestDTO;
import com.golden.erp.dto.cliente.ClienteResponseDTO;
import com.golden.erp.infrastructure.integration.cep.ViaCepService;
import com.golden.erp.interfaces.ClienteService;
import com.golden.erp.infrastructure.repository.ClienteRepository;
import com.golden.erp.mapper.ClienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);

    private final ClienteRepository clienteRepository;
    private final ViaCepService viaCepService;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ViaCepService viaCepService, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.viaCepService = viaCepService;
        this.clienteMapper = clienteMapper;
    }

    public ClienteResponseDTO criar(ClienteRequestDTO request) {
        logger.info("Criando cliente: {}", request.getNome());

        validarUnicidadeEmail(request.getEmail(), null);
        validarUnicidadeCpf(request.getCpf(), null);

        request.setEndereco(viaCepService.preencherEnderecoComViaCep(request.getEndereco()));

        Cliente cliente = clienteMapper.toEntity(request);

        Cliente clienteSalvo = clienteRepository.save(cliente);
        logger.info("Cliente criado com sucesso. ID: {}", clienteSalvo.getId());

        return clienteMapper.toResponseDTO(clienteSalvo);
    }

    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO request) {
        logger.info("Atualizando cliente ID: {}", id);

        Cliente cliente = buscarClientePorId(id);

        validarUnicidadeEmail(request.getEmail(), id);
        validarUnicidadeCpf(request.getCpf(), id);

        request.setEndereco(viaCepService.preencherEnderecoComViaCep(request.getEndereco()));

        clienteMapper.updateEntityFromDTO(request, cliente);

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        logger.info("Cliente atualizado com sucesso. ID: {}", clienteAtualizado.getId());

        return clienteMapper.toResponseDTO(clienteAtualizado);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        logger.info("Buscando cliente por ID: {}", id);
        Cliente cliente = buscarClientePorId(id);
        return clienteMapper.toResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(String nome, Pageable pageable) {
        logger.info("Listando clientes com filtro nome: {}", nome);
        Page<Cliente> clientes = clienteRepository.findByFilters(nome, pageable);
        return clientes.map(clienteMapper::toResponseDTO);
    }

    public void excluir(Long id) {
        logger.info("Executando soft delete do cliente ID: {}", id);
        Cliente cliente = buscarClientePorId(id);
        cliente.markAsDeleted();
        clienteRepository.save(cliente);
        logger.info("Cliente marcado como excluído com sucesso. ID: {}", id);
    }

    private Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + id));
    }

    private void validarUnicidadeEmail(String email, Long idExcluir) {
        boolean emailJaExiste = clienteRepository.existsByEmail(email);

        if (emailJaExiste) {
            if (idExcluir != null) {
                Cliente clienteComEmail = clienteRepository.findByEmail(email).orElse(null);
                if (clienteComEmail != null && !clienteComEmail.getId().equals(idExcluir)) {
                    throw new RuntimeException("Email já está em uso por outro cliente");
                }
            } else {
                throw new RuntimeException("Email já está em uso");
            }
        }
    }

    private void validarUnicidadeCpf(String cpf, Long idExcluir) {
        boolean cpfJaExiste = clienteRepository.existsByCpf(cpf);

        if (cpfJaExiste) {
            if (idExcluir != null) {
                Cliente clienteComCpf = clienteRepository.findByCpf(cpf).orElse(null);
                if (clienteComCpf != null && !clienteComCpf.getId().equals(idExcluir)) {
                    throw new RuntimeException("CPF já está em uso por outro cliente");
                }
            } else {
                throw new RuntimeException("CPF já está em uso");
            }
        }
    }
}