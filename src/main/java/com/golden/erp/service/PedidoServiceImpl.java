package com.golden.erp.service;

import com.golden.erp.dto.pedido.*;
import com.golden.erp.domain.*;
import com.golden.erp.infrastructure.repository.ClienteRepository;
import com.golden.erp.infrastructure.repository.PedidoRepository;
import com.golden.erp.infrastructure.repository.ProdutoRepository;
import com.golden.erp.interfaces.PedidoService;
import com.golden.erp.mapper.PedidoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                           ClienteRepository clienteRepository,
                           ProdutoRepository produtoRepository,
                           PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Override
    public PedidoResponseDTO criar(PedidoRequestDTO request) {
        logger.info("Criando pedido para cliente ID: {}", request.getClienteId());

        // Validar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar produtos e verificar estoque
        Map<Long, Produto> produtos = validarProdutosEEstoque(request.getItens());

        // Criar pedido
        Pedido pedido = new Pedido(cliente);

        // Criar itens do pedido
        List<ItemPedido> itens = criarItensPedido(pedido, request.getItens(), produtos);
        pedido.setItens(itens);

        // Calcular totais
        calcularTotais(pedido);

        // Baixar estoque
        baixarEstoque(request.getItens(), produtos);

        // Salvar pedido
        pedido = pedidoRepository.save(pedido);

        logger.info("Pedido criado com sucesso. ID: {}", pedido.getId());
        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        logger.info("Buscando pedido por ID: {}", id);

        Pedido pedido = pedidoRepository.findByIdWithFullDetails(id);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado");
        }

        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    public Page<PedidoResponseDTO> listar(Long clienteId, StatusPedido status, Pageable pageable) {
        logger.info("Listando pedidos com filtros - clienteId: {}, status: {}", clienteId, status);

        Page<Pedido> pedidos = pedidoRepository.findByFilters(clienteId, status, pageable);
        return pedidos.map(pedidoMapper::toResponseDTO);
    }

    @Override
    public PedidoResponseDTO pagar(Long id) {
        logger.info("Processando pagamento do pedido ID: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        try {
            pedido.pagar();
            pedido = pedidoRepository.save(pedido);

            logger.info("Pedido pago com sucesso. ID: {}", id);
            return pedidoMapper.toResponseDTO(pedido);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Não é possível pagar o pedido: " + e.getMessage());
        }
    }

    @Override
    public PedidoResponseDTO cancelar(Long id) {
        logger.info("Cancelando pedido ID: {}", id);

        Pedido pedido = pedidoRepository.findByIdWithFullDetails(id);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado");
        }

        if (!pedido.podeSerCancelado()) {
            throw new RuntimeException("Pedido já pago não pode ser cancelado");
        }

        // Devolver estoque se não estiver pago
        if (!pedido.estaPago()) {
            devolverEstoque(pedido.getItens());
        }

        pedido.cancelar();
        pedido = pedidoRepository.save(pedido);

        logger.info("Pedido cancelado com sucesso. ID: {}", id);
        return pedidoMapper.toResponseDTO(pedido);
    }

    @Override
    public void excluir(Long id) {
        logger.info("Excluindo pedido ID: {}", id);

        Pedido pedido = pedidoRepository.findByIdWithFullDetails(id);
        if (pedido == null) {
            throw new RuntimeException("Pedido não encontrado");
        }

        // Devolver estoque se não estiver pago
        if (!pedido.estaPago()) {
            devolverEstoque(pedido.getItens());
        }

        pedido.markAsDeleted();
        pedidoRepository.save(pedido);

        logger.info("Pedido excluído com sucesso. ID: {}", id);
    }

    private Map<Long, Produto> validarProdutosEEstoque(List<ItemPedidoRequestDTO> itens) {
        // Agrupar produtos duplicados e somar quantidades
        Map<Long, Integer> quantidadePorProduto = itens.stream()
                .collect(Collectors.groupingBy(
                    ItemPedidoRequestDTO::getProdutoId,
                    Collectors.summingInt(ItemPedidoRequestDTO::getQuantidade)
                ));

        List<Long> produtoIds = new ArrayList<>(quantidadePorProduto.keySet());

        Map<Long, Produto> produtos = produtoRepository.findAllById(produtoIds)
                .stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));

        // Verificar se todos os produtos foram encontrados
        List<Long> produtosNaoEncontrados = produtoIds.stream()
                .filter(id -> !produtos.containsKey(id))
                .toList();

        if (!produtosNaoEncontrados.isEmpty()) {
            throw new RuntimeException("Produtos não encontrados: " + produtosNaoEncontrados);
        }

        // Verificar se produtos estão ativos
        List<String> produtosInativos = produtos.values().stream()
                .filter(p -> !p.getAtivo())
                .map(Produto::getNome)
                .toList();

        if (!produtosInativos.isEmpty()) {
            throw new RuntimeException("Produtos inativos: " + produtosInativos);
        }

        // Verificar estoque considerando agrupamento
        List<String> produtosSemEstoque = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantidadePorProduto.entrySet()) {
            Long produtoId = entry.getKey();
            Integer quantidadeTotal = entry.getValue();
            Produto produto = produtos.get(produtoId);

            if (produto.getEstoque() < quantidadeTotal) {
                produtosSemEstoque.add(String.format("%s (disponível: %d, solicitado: %d)",
                    produto.getNome(), produto.getEstoque(), quantidadeTotal));
            }
        }

        if (!produtosSemEstoque.isEmpty()) {
            throw new RuntimeException("Estoque insuficiente para: " + produtosSemEstoque);
        }

        return produtos;
    }

    private List<ItemPedido> criarItensPedido(Pedido pedido, List<ItemPedidoRequestDTO> itensRequest,
                                             Map<Long, Produto> produtos) {
        List<ItemPedido> itens = new ArrayList<>();

        for (ItemPedidoRequestDTO itemRequest : itensRequest) {
            Produto produto = produtos.get(itemRequest.getProdutoId());
            ItemPedido item = new ItemPedido(pedido, produto, itemRequest.getQuantidade(),
                                           itemRequest.getDesconto());
            itens.add(item);
        }

        return itens;
    }

    private void calcularTotais(Pedido pedido) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDesconto = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            subtotal = subtotal.add(item.getValorBruto());
            if (item.getDesconto() != null) {
                totalDesconto = totalDesconto.add(item.getDesconto());
            }
        }

        BigDecimal total = subtotal.subtract(totalDesconto);

        // Arredondamento seguro com 2 casas decimais
        pedido.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        pedido.setTotalDesconto(totalDesconto.setScale(2, RoundingMode.HALF_UP));
        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    private void baixarEstoque(List<ItemPedidoRequestDTO> itens, Map<Long, Produto> produtos) {
        // Agrupar por produto para somar quantidades em caso de produtos duplicados no pedido
        Map<Long, Integer> quantidadePorProduto = itens.stream()
                .collect(Collectors.groupingBy(
                    ItemPedidoRequestDTO::getProdutoId,
                    Collectors.summingInt(ItemPedidoRequestDTO::getQuantidade)
                ));

        // Atualizar estoque diretamente no banco usando batch update
        for (Map.Entry<Long, Integer> entry : quantidadePorProduto.entrySet()) {
            produtoRepository.baixarEstoque(entry.getKey(), entry.getValue());
        }
    }

    private void devolverEstoque(List<ItemPedido> itens) {
        // Agrupar por produto para somar quantidades em caso de produtos duplicados no pedido
        Map<Long, Integer> quantidadePorProduto = itens.stream()
                .collect(Collectors.groupingBy(
                    item -> item.getProduto().getId(),
                    Collectors.summingInt(ItemPedido::getQuantidade)
                ));

        // Devolver estoque diretamente no banco usando batch update
        for (Map.Entry<Long, Integer> entry : quantidadePorProduto.entrySet()) {
            produtoRepository.devolverEstoque(entry.getKey(), entry.getValue());
        }
    }
}
