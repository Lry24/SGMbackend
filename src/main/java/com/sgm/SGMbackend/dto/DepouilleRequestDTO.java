package com.sgm.SGMbackend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DepouilleRequestDTO {

    private String nomDefunt;
    private String prenomDefunt;
    private LocalDate dateDeces;
    private String causePresumee;
    private String provenance;
}