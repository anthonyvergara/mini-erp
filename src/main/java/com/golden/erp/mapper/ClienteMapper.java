package com.golden.erp.mapper;

import com.golden.erp.domain.Cliente;
import com.golden.erp.dto.cliente.ClienteRequestDTO;
import com.golden.erp.dto.cliente.ClienteResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = EnderecoMapper.class)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Cliente toEntity(ClienteRequestDTO requestDTO);

    ClienteResponseDTO toResponseDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(ClienteRequestDTO requestDTO, @MappingTarget Cliente cliente);
}
