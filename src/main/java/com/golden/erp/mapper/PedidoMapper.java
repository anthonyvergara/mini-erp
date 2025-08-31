package com.golden.erp.mapper;

import com.golden.erp.domain.Pedido;
import com.golden.erp.dto.pedido.PedidoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ItemPedidoMapper.class)
public interface PedidoMapper {

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nome", target = "clienteNome")
    PedidoResponseDTO toResponseDTO(Pedido pedido);
}

