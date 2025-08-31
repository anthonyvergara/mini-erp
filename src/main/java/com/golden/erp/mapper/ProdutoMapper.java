package com.golden.erp.mapper;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.produto.ProdutoRequestDTO;
import com.golden.erp.dto.produto.ProdutoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProdutoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Produto toEntity(ProdutoRequestDTO requestDTO);

    ProdutoResponseDTO toResponseDTO(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(ProdutoRequestDTO requestDTO, @MappingTarget Produto produto);
}

