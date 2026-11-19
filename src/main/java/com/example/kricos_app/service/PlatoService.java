package com.example.kricos_app.service;

import com.example.kricos_app.dto.PlatoDTO;
import java.util.List;

public interface PlatoService {
    List<PlatoDTO> listarPlatos(String categoria, String q);
    List<String> listarCategorias();
    PlatoDTO obtenerPorId(Long id);
}
