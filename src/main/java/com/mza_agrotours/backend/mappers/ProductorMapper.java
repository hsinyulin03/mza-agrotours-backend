package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.productor.ProductorGetDTO;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.productor.ProductorEstado;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductorMapper {

    default List<ProductorGetDTO> productorListToProductorGetDTOList(List<Productor> productores) {
        return productores.stream().map(this::productorToProductorGetDTO).toList();
    }

    default ProductorGetDTO productorToProductorGetDTO(Productor productor) {
        ProductorGetDTO dto = new ProductorGetDTO();
        dto.setId(productor.getId().toString());
        dto.setFechaHoraAlta(productor.getFechaHoraAlta());

        // Data usuario
        dto.setNombreUsuario(productor.getUsuario().getNombre());
        dto.setEmailUsuario(productor.getUsuario().getEmail());
        dto.setIdentificacion(productor.getUsuario().getIdentificacion());

        // Data rol. El unico rol protegido dentro del scope de un establecimiento
        // es el de Productor Lider.
        dto.setNombreRol(productor.getRol().getNombre());
        dto.setEsLider(Boolean.TRUE.equals(productor.getRol().getEsProtegido()));

        // Data estado
        dto.setEstadoActual(productor.getEstadoActual().getNombre().name());
        ProductorEstado ultimoEstado = productor.getEstados().stream()
                .filter(tramo -> tramo.getFechaHoraFin() == null)
                .findFirst()
                .orElse(null);

        if (ultimoEstado != null && ultimoEstado.getEstadoProductor().getNombre().equals(EstadoProductorNombre.LICENCIA)) {
            dto.setFechaHoraFinSuspension(ultimoEstado.getFechaHoraFinPrevista());
            dto.setMotivoSuspension(ultimoEstado.getMotivo());
            dto.setFechaHoraInicioSuspension(ultimoEstado.getFechaHoraInicio());
        }

        return dto;
    }
}
