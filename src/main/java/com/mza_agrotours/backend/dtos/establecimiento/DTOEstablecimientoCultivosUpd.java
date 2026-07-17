package com.mza_agrotours.backend.dtos.establecimiento;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTOEstablecimientoCultivosUpd {
    private List<UUID> cultivosIds;


}
