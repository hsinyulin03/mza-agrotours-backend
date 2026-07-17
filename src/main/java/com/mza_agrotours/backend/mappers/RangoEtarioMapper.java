package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.rangoEtario.DTORangoEtarioGet;
import com.mza_agrotours.backend.entities.RangoEtario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class RangoEtarioMapper {
    public abstract DTORangoEtarioGet rangoEtariotoDTORangoEtarioGet(RangoEtario rango);
    public abstract List<DTORangoEtarioGet> rangoEtarioListtoDTORangoEtarioGetList(List<RangoEtario> rangos);
}
