package com.example.kricos_app.mapper;

import com.example.kricos_app.dto.PlatoDTO;
import com.example.kricos_app.model.Plato;

public class PlatoMapper {
    public static PlatoDTO aDTO(Plato plato) {
        if (plato == null) return null;
        return new PlatoDTO(
            plato.getId(),
            plato.getCategoria(),
            plato.getNombre(),
            plato.getPrecio(),
            plato.getDescripcion(),
            plato.isDisponible()
        );
    }
}
