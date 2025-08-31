package com.golden.erp.mapper;

import com.golden.erp.domain.ItemPedido;
import com.golden.erp.dto.pedido.ItemPedidoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemPedidoMapper {

    @Mapping(source = "produto.id", target = "produtoId")
    @Mapping(source = "produto.nome", target = "produtoNome")
    ItemPedidoResponseDTO toResponseDTO(ItemPedido itemPedido);
}

