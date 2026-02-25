package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UtilisateurMapper {

    UtilisateurResponseDTO toResponseDTO(Utilisateur entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "doitChangerMotDePasse", ignore = true)
    @Mapping(target = "restitutionsValidees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Utilisateur toEntity(UtilisateurRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "doitChangerMotDePasse", ignore = true)
    @Mapping(target = "restitutionsValidees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UtilisateurRequestDTO requestDTO, @MappingTarget Utilisateur entity);
}
