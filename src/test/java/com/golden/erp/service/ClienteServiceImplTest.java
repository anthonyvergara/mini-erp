package com.golden.erp.service;

import com.golden.erp.domain.Cliente;
import com.golden.erp.domain.Endereco;
import com.golden.erp.dto.cliente.ClienteRequestDTO;
import com.golden.erp.dto.cliente.ClienteResponseDTO;
import com.golden.erp.dto.cliente.EnderecoRequestDTO;
import com.golden.erp.dto.cliente.EnderecoResponseDTO;
import com.golden.erp.infrastructure.integration.cep.ViaCepService;
import com.golden.erp.infrastructure.repository.ClienteRepository;
import com.golden.erp.mapper.ClienteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - Testes Unitários")
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ViaCepService viaCepService;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequestDTO clienteRequestDTO;
    private Cliente cliente;
    private ClienteResponseDTO clienteResponseDTO;
    private EnderecoRequestDTO enderecoRequestDTO;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        // Setup dos dados de teste
        enderecoRequestDTO = new EnderecoRequestDTO();
        enderecoRequestDTO.setCep("12345678");
        enderecoRequestDTO.setLogradouro("Rua Teste");
        enderecoRequestDTO.setNumero("123");
        enderecoRequestDTO.setBairro("Bairro Teste");
        enderecoRequestDTO.setCidade("Cidade Teste");
        enderecoRequestDTO.setUf("SP");

        endereco = new Endereco();
        endereco.setCep("12345678");
        endereco.setLogradouro("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Cidade Teste");
        endereco.setUf("SP");

        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNome("João Silva");
        clienteRequestDTO.setEmail("joao@email.com");
        clienteRequestDTO.setCpf("12345678901");
        clienteRequestDTO.setEndereco(enderecoRequestDTO);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678901");
        cliente.setEndereco(endereco);

        EnderecoResponseDTO enderecoResponseDTO = new EnderecoResponseDTO();
        enderecoResponseDTO.setCep("12345678");
        enderecoResponseDTO.setLogradouro("Rua Teste");
        enderecoResponseDTO.setNumero("123");
        enderecoResponseDTO.setBairro("Bairro Teste");
        enderecoResponseDTO.setCidade("Cidade Teste");
        enderecoResponseDTO.setUf("SP");

        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(1L);
        clienteResponseDTO.setNome("João Silva");
        clienteResponseDTO.setEmail("joao@email.com");
        clienteResponseDTO.setCpf("12345678901");
        clienteResponseDTO.setEndereco(enderecoResponseDTO);
        clienteResponseDTO.setCreatedAt(LocalDateTime.now());
        clienteResponseDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Testes para criação de cliente")
    class CriarClienteTests {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void deveCriarClienteComSucesso() {
            // Arrange
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(false);
            when(viaCepService.preencherEnderecoComViaCep(enderecoRequestDTO)).thenReturn(enderecoRequestDTO);
            when(clienteMapper.toEntity(clienteRequestDTO)).thenReturn(cliente);
            when(clienteRepository.save(cliente)).thenReturn(cliente);
            when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

            // Act
            ClienteResponseDTO resultado = clienteService.criar(clienteRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("João Silva");
            assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
            assertThat(resultado.getCpf()).isEqualTo("12345678901");

            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).existsByCpf(clienteRequestDTO.getCpf());
            verify(viaCepService).preencherEnderecoComViaCep(enderecoRequestDTO);
            verify(clienteMapper).toEntity(clienteRequestDTO);
            verify(clienteRepository).save(cliente);
            verify(clienteMapper).toResponseDTO(cliente);
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clienteService.criar(clienteRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email já está em uso");

            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository, never()).existsByCpf(any());
            verify(viaCepService, never()).preencherEnderecoComViaCep(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF já existe")
        void deveLancarExcecaoQuandoCpfJaExiste() {
            // Arrange
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clienteService.criar(clienteRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("CPF já está em uso");

            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).existsByCpf(clienteRequestDTO.getCpf());
            verify(viaCepService, never()).preencherEnderecoComViaCep(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve chamar preenchimento de endereço via CEP")
        void deveChamarPreenchimentoDeEnderecoViaCep() {
            // Arrange
            when(clienteRepository.existsByEmail(any())).thenReturn(false);
            when(clienteRepository.existsByCpf(any())).thenReturn(false);
            when(viaCepService.preencherEnderecoComViaCep(enderecoRequestDTO)).thenReturn(enderecoRequestDTO);
            when(clienteMapper.toEntity(any())).thenReturn(cliente);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteMapper.toResponseDTO(any())).thenReturn(clienteResponseDTO);

            // Act
            clienteService.criar(clienteRequestDTO);

            // Assert
            ArgumentCaptor<EnderecoRequestDTO> enderecoCaptor = ArgumentCaptor.forClass(EnderecoRequestDTO.class);
            verify(viaCepService).preencherEnderecoComViaCep(enderecoCaptor.capture());
            assertThat(enderecoCaptor.getValue()).isEqualTo(enderecoRequestDTO);
        }
    }

    @Nested
    @DisplayName("Testes para atualização de cliente")
    class AtualizarClienteTests {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void deveAtualizarClienteComSucesso() {
            // Arrange
            Long clienteId = 1L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(true);
            when(clienteRepository.findByEmail(clienteRequestDTO.getEmail())).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(true);
            when(clienteRepository.findByCpf(clienteRequestDTO.getCpf())).thenReturn(Optional.of(cliente));
            when(viaCepService.preencherEnderecoComViaCep(enderecoRequestDTO)).thenReturn(enderecoRequestDTO);
            when(clienteRepository.save(cliente)).thenReturn(cliente);
            when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

            // Act
            ClienteResponseDTO resultado = clienteService.atualizar(clienteId, clienteRequestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(clienteId);

            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).findByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).existsByCpf(clienteRequestDTO.getCpf());
            verify(clienteRepository).findByCpf(clienteRequestDTO.getCpf());
            verify(viaCepService).preencherEnderecoComViaCep(enderecoRequestDTO);
            verify(clienteMapper).updateEntityFromDTO(clienteRequestDTO, cliente);
            verify(clienteRepository).save(cliente);
            verify(clienteMapper).toResponseDTO(cliente);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            // Arrange
            Long clienteId = 999L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clienteService.atualizar(clienteId, clienteRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cliente não encontrado com ID: " + clienteId);

            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository, never()).existsByEmail(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email pertence a outro cliente")
        void deveLancarExcecaoQuandoEmailPertenceAOutroCliente() {
            // Arrange
            Long clienteId = 1L;
            Cliente outroCliente = new Cliente();
            outroCliente.setId(2L);
            outroCliente.setEmail(clienteRequestDTO.getEmail());

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(true);
            when(clienteRepository.findByEmail(clienteRequestDTO.getEmail())).thenReturn(Optional.of(outroCliente));

            // Act & Assert
            assertThatThrownBy(() -> clienteService.atualizar(clienteId, clienteRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Email já está em uso por outro cliente");

            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).findByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF pertence a outro cliente")
        void deveLancarExcecaoQuandoCpfPertenceAOutroCliente() {
            // Arrange
            Long clienteId = 1L;
            Cliente outroCliente = new Cliente();
            outroCliente.setId(2L);
            outroCliente.setCpf(clienteRequestDTO.getCpf());

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(true);
            when(clienteRepository.findByCpf(clienteRequestDTO.getCpf())).thenReturn(Optional.of(outroCliente));

            // Act & Assert
            assertThatThrownBy(() -> clienteService.atualizar(clienteId, clienteRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("CPF já está em uso por outro cliente");

            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).existsByEmail(clienteRequestDTO.getEmail());
            verify(clienteRepository).existsByCpf(clienteRequestDTO.getCpf());
            verify(clienteRepository).findByCpf(clienteRequestDTO.getCpf());
            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para busca de cliente")
    class BuscarClienteTests {

        @Test
        @DisplayName("Deve buscar cliente por ID com sucesso")
        void deveBuscarClientePorIdComSucesso() {
            // Arrange
            Long clienteId = 1L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

            // Act
            ClienteResponseDTO resultado = clienteService.buscarPorId(clienteId);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(clienteId);
            assertThat(resultado.getNome()).isEqualTo("João Silva");
            assertThat(resultado.getEmail()).isEqualTo("joao@email.com");

            verify(clienteRepository).findById(clienteId);
            verify(clienteMapper).toResponseDTO(cliente);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontradoNaBusca() {
            // Arrange
            Long clienteId = 999L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clienteService.buscarPorId(clienteId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cliente não encontrado com ID: " + clienteId);

            verify(clienteRepository).findById(clienteId);
            verify(clienteMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para listagem de clientes")
    class ListarClientesTests {

        @Test
        @DisplayName("Deve listar clientes com filtro de nome")
        void deveListarClientesComFiltroDeNome() {
            // Arrange
            String nomeFiltro = "João";
            Pageable pageable = PageRequest.of(0, 10);
            List<Cliente> clientes = List.of(cliente);
            Page<Cliente> pageClientes = new PageImpl<>(clientes, pageable, 1);

            when(clienteRepository.findByFilters(nomeFiltro, pageable)).thenReturn(pageClientes);
            when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

            // Act
            Page<ClienteResponseDTO> resultado = clienteService.listar(nomeFiltro, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("João Silva");
            assertThat(resultado.getTotalElements()).isEqualTo(1);

            verify(clienteRepository).findByFilters(nomeFiltro, pageable);
            verify(clienteMapper).toResponseDTO(cliente);
        }

        @Test
        @DisplayName("Deve listar clientes sem filtro")
        void deveListarClientesSemFiltro() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Cliente> clientes = List.of(cliente);
            Page<Cliente> pageClientes = new PageImpl<>(clientes, pageable, 1);

            when(clienteRepository.findByFilters(null, pageable)).thenReturn(pageClientes);
            when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

            // Act
            Page<ClienteResponseDTO> resultado = clienteService.listar(null, pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);

            verify(clienteRepository).findByFilters(null, pageable);
            verify(clienteMapper).toResponseDTO(cliente);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há clientes")
        void deveRetornarPaginaVaziaQuandoNaoHaClientes() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cliente> pageVazia = new PageImpl<>(List.of(), pageable, 0);

            when(clienteRepository.findByFilters(any(), eq(pageable))).thenReturn(pageVazia);

            // Act
            Page<ClienteResponseDTO> resultado = clienteService.listar("Nome Inexistente", pageable);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).isEmpty();
            assertThat(resultado.getTotalElements()).isEqualTo(0);

            verify(clienteRepository).findByFilters("Nome Inexistente", pageable);
            verify(clienteMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para exclusão de cliente")
    class ExcluirClienteTests {

        @Test
        @DisplayName("Deve excluir cliente com sucesso (soft delete)")
        void deveExcluirClienteComSucesso() {
            // Arrange
            Long clienteId = 1L;
            Cliente clienteParaExcluir = spy(cliente);
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteParaExcluir));
            when(clienteRepository.save(clienteParaExcluir)).thenReturn(clienteParaExcluir);

            // Act
            clienteService.excluir(clienteId);

            // Assert
            verify(clienteRepository).findById(clienteId);
            verify(clienteParaExcluir).markAsDeleted();
            verify(clienteRepository).save(clienteParaExcluir);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado para exclusão")
        void deveLancarExcecaoQuandoClienteNaoEncontradoParaExclusao() {
            // Arrange
            Long clienteId = 999L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clienteService.excluir(clienteId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cliente não encontrado com ID: " + clienteId);

            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes para validações de unicidade")
    class ValidacoesUnicidadeTests {

        @Test
        @DisplayName("Deve permitir atualização com mesmo email do próprio cliente")
        void devePermitirAtualizacaoComMesmoEmailDoProprioCliente() {
            // Arrange
            Long clienteId = 1L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(true);
            when(clienteRepository.findByEmail(clienteRequestDTO.getEmail())).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(false);
            when(viaCepService.preencherEnderecoComViaCep(any())).thenReturn(enderecoRequestDTO);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteMapper.toResponseDTO(any())).thenReturn(clienteResponseDTO);

            // Act & Assert
            assertThatCode(() -> clienteService.atualizar(clienteId, clienteRequestDTO))
                    .doesNotThrowAnyException();

            verify(clienteRepository).findByEmail(clienteRequestDTO.getEmail());
        }

        @Test
        @DisplayName("Deve permitir atualização com mesmo CPF do próprio cliente")
        void devePermitirAtualizacaoComMesmoCpfDoProprioCliente() {
            // Arrange
            Long clienteId = 1L;
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteRepository.existsByEmail(clienteRequestDTO.getEmail())).thenReturn(false);
            when(clienteRepository.existsByCpf(clienteRequestDTO.getCpf())).thenReturn(true);
            when(clienteRepository.findByCpf(clienteRequestDTO.getCpf())).thenReturn(Optional.of(cliente));
            when(viaCepService.preencherEnderecoComViaCep(any())).thenReturn(enderecoRequestDTO);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteMapper.toResponseDTO(any())).thenReturn(clienteResponseDTO);

            // Act & Assert
            assertThatCode(() -> clienteService.atualizar(clienteId, clienteRequestDTO))
                    .doesNotThrowAnyException();

            verify(clienteRepository).findByCpf(clienteRequestDTO.getCpf());
        }
    }
}
