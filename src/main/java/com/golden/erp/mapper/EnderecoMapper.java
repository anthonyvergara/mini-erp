package com.golden.erp.mapper;

import com.golden.erp.domain.Endereco;
import com.golden.erp.dto.cliente.EnderecoRequestDTO;
import com.golden.erp.dto.cliente.EnderecoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnderecoMapper {

    Endereco toEntity(EnderecoRequestDTO requestDTO);

    EnderecoResponseDTO toResponseDTO(Endereco endereco);
}
