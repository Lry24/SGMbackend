package com.sgm.SGMbackend.dto.dtoRequest;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteConfigRequestDTO {
    @NotNull(message = "Le type est obligatoire")
    private TypeAlerte type;

    @NotNull(message = "Le seuil est obligatoire")
    private Float seuil;

    @NotBlank(message = "Le canal est obligatoire")
    private String canal;

    private String destinataires;
}
