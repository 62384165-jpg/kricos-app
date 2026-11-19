package com.example.kricos_app.service;

import com.example.kricos_app.dto.PlatoDTO;
import com.example.kricos_app.exception.RecursoNoEncontradoException;
import com.example.kricos_app.mapper.PlatoMapper;
import com.example.kricos_app.model.Plato;
import com.example.kricos_app.repository.PlatoRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;

    public PlatoServiceImpl(PlatoRepository platoRepository) {
        this.platoRepository = platoRepository;
    }

    @Override
    public List<PlatoDTO> listarPlatos(String categoria, String q) {
        List<Plato> platos = platoRepository.findAll();

        final String catFiltro = categoria != null ? categoria.trim() : "";
        final String qFiltro = q != null ? normalizarTexto(q.trim()) : "";

        return platos.stream()
                .filter(p -> catFiltro.isEmpty() || p.getCategoria().equalsIgnoreCase(catFiltro))
                .filter(p -> {
                    if (qFiltro.isEmpty()) return true;
                    String textoPlato = normalizarTexto(p.getNombre() + " " + p.getDescripcion());
                    return textoPlato.contains(qFiltro);
                })
                .map(PlatoMapper::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listarCategorias() {
        return platoRepository.findCategorias();
    }

    @Override
    public PlatoDTO obtenerPorId(Long id) {
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el plato con id " + id));
        return PlatoMapper.aDTO(plato);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }
}
