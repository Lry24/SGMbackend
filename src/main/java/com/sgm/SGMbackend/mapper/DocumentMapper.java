package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.DocumentRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.DocumentResponseDTO;
import com.sgm.SGMbackend.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocumentResponseDTO toResponseDTO(Document entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Document toEntity(DocumentRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(DocumentRequestDTO requestDTO, @MappingTarget Document entity);
}
